package pl.yalgrin.playnite.simplesync.client.service

import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import pl.yalgrin.playnite.simplesync.change.mapper.ChangeMessageMapper
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.dto.SessionSettingsDTO
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.client.message.ConnectionMessage
import pl.yalgrin.playnite.simplesync.client.message.InitializationMessage
import pl.yalgrin.playnite.simplesync.common.util.thenAny
import pl.yalgrin.playnite.simplesync.security.SessionManager
import pl.yalgrin.playnite.simplesync.security.getSessionInfo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.util.*

@Service
class ConnectionService(
    private val sessionManager: SessionManager,
    private val registeredClientService: RegisteredClientService,
    private val changeListenerService: ChangeListenerService,
    private val changeMessageMapper: ChangeMessageMapper
) {
    fun connect(): Flux<ServerSentEvent<ConnectionMessage>> {
        return createSession()
            .flatMapMany { sessionInfo ->
                Flux.mergeSequential(
                    createInitializationMessage(sessionInfo),
                    Flux.merge(
                        createHeartbeatStream(),
                        createChangeMessageStream(sessionInfo)
                    )
                ).handleSessionLifecycle(sessionInfo)
            }
    }

    fun connectPanelUser(): Flux<ServerSentEvent<List<ConnectionMessage>>> {
        return createSession()
            .flatMapMany { sessionInfo ->
                Flux.mergeSequential(
                    createInitializationMessageList(sessionInfo),
                    Flux.merge(
                        createHeartbeatStream(),
                        createBufferedChangeMessageStream(sessionInfo)
                    )
                ).handleSessionLifecycle(sessionInfo)
            }
    }

    private fun createSession(): Mono<SessionInfoDTO> {
        return getSessionInfo()
            .map { sessionInfo ->
                sessionInfo.copy(sessionId = UUID.randomUUID().toString())
            }
            .flatMap { sessionInfo ->
                if (!sessionInfo.panelUser) {
                    registeredClientService.updateConnectedTime(sessionInfo.clientId)
                        .thenReturn(sessionInfo)
                } else {
                    sessionInfo.toMono()
                }
            }
    }

    private fun createInitializationMessage(sessionInfo: SessionInfoDTO): Mono<ServerSentEvent<ConnectionMessage>> =
        Mono.fromSupplier { InitializationMessage(sessionInfo.sessionId) }
            .map { ServerSentEvent.builder<ConnectionMessage>().data(it).build() }

    private fun createInitializationMessageList(sessionInfo: SessionInfoDTO): Mono<ServerSentEvent<List<ConnectionMessage>>> =
        Mono.fromSupplier { InitializationMessage(sessionInfo.sessionId) }
            .map { ServerSentEvent.builder<List<ConnectionMessage>>().data(listOf(it)).build() }

    private fun <T : Any> createHeartbeatStream(): Flux<ServerSentEvent<T>> =
        Flux.interval(Duration.ofSeconds(30)).map { createHeartbeat() }

    private fun <T : Any> createHeartbeat(): ServerSentEvent<T> {
        return ServerSentEvent.builder<T>().comment("heartbeat").build()
    }

    private fun createChangeMessageStream(sessionInfo: SessionInfoDTO): Flux<ServerSentEvent<ConnectionMessage>> =
        createChangeStream(sessionInfo).map { ServerSentEvent.builder<ConnectionMessage>().data(it).build() }

    private fun createBufferedChangeMessageStream(sessionInfo: SessionInfoDTO): Flux<ServerSentEvent<List<ConnectionMessage>>> =
        createChangeStream(sessionInfo)
            .buffer(Duration.ofMillis(300))
            .map { ServerSentEvent.builder<List<ConnectionMessage>>().data(it).build() }

    private fun createChangeStream(sessionInfo: SessionInfoDTO): Flux<ChangeMessage> =
        changeListenerService.modificationFlux()
            .filter { it.isForceFetch || it.clientId != sessionInfo.clientId }.filterWhen {
                sessionManager.getSessionSettingsMono(sessionInfo.sessionId)
                    .map { it.enabledChangeStream }
                    .defaultIfEmpty(false)
            }.map {
                changeMessageMapper.toMessage(it)
            }

    private fun <T : Any> Flux<T>.handleSessionLifecycle(sessionInfo: SessionInfoDTO): Flux<T> {
        return this.doOnSubscribe {
            sessionManager.saveSessionInfo(sessionInfo)
            if (sessionInfo.panelUser) {
                val settings =
                    sessionManager.getSessionSettings(sessionInfo.sessionId) ?: SessionSettingsDTO()
                sessionManager.updateSettings(sessionInfo, settings.copy(enabledChangeStream = true))
            }
        }
            .doOnTerminate { sessionManager.removeSessionInfo(sessionInfo) }
            .doOnCancel { sessionManager.removeSessionInfo(sessionInfo) }
    }

    fun enableChangeStream(): Mono<Unit> {
        return getSessionInfo()
            .flatMap { sessionInfo ->
                sessionManager.getSessionSettingsMono(sessionInfo.sessionId)
                    .doOnNext { settings ->
                        sessionManager.updateSettings(sessionInfo, settings.copy(enabledChangeStream = true))
                    }
            }.thenAny()
    }

    fun disableChangeStream(): Mono<Unit> {
        return getSessionInfo()
            .flatMap { sessionInfo ->
                sessionManager.getSessionSettingsMono(sessionInfo.sessionId)
                    .doOnNext { settings ->
                        sessionManager.updateSettings(sessionInfo, settings.copy(enabledChangeStream = false))
                    }
            }.thenAny()
    }
}