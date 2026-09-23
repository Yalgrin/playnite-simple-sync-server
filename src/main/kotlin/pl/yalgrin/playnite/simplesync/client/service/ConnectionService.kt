package pl.yalgrin.playnite.simplesync.client.service

import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import pl.yalgrin.playnite.simplesync.change.mapper.ChangeMessageMapper
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.message.ConnectionMessage
import pl.yalgrin.playnite.simplesync.client.message.InitializationMessage
import pl.yalgrin.playnite.simplesync.common.util.thenAny
import pl.yalgrin.playnite.simplesync.security.SessionManager
import pl.yalgrin.playnite.simplesync.security.getSessionInfo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Service
class ConnectionService(
    private val sessionManager: SessionManager,
    private val registeredClientService: RegisteredClientService,
    private val changeListenerService: ChangeListenerService,
    private val changeMessageMapper: ChangeMessageMapper
) {
    //TODO remove the forceOpen flag
    fun connect(forceOpen: Boolean = false): Flux<ServerSentEvent<ConnectionMessage>> {
        return createSession()
            .flatMap { sessionInfo ->
                registeredClientService.updateConnectedTime(sessionInfo.clientId)
                    .thenReturn(sessionInfo)
            }
            .flatMapMany { sessionInfo ->
                Flux.mergeSequential(
                    Mono.fromSupplier { InitializationMessage(sessionInfo.sessionId) }
                        .map { ServerSentEvent.builder<ConnectionMessage>().data(it).build() },
                    Flux.merge(
                        Flux.interval(Duration.ofSeconds(30)).map { createHeartbeat() },
                        changeListenerService.modificationFlux()
                            .filter { forceOpen || it.isForceFetch || it.clientId != sessionInfo.clientId }.filterWhen {
                                if (forceOpen)
                                    Mono.just(true)
                                else
                                    sessionManager.getSessionSettingsMono(sessionInfo.sessionId)
                                        .map { it.enabledChangeStream }
                                        .defaultIfEmpty(false)
                            }.map {
                            ServerSentEvent.builder<ConnectionMessage>().data(
                                changeMessageMapper.toMessage(it)
                            ).build()
                        }
                    )
                )
                    .doOnSubscribe { sessionManager.saveSessionInfo(sessionInfo) }
                    .doOnTerminate { sessionManager.removeSessionInfo(sessionInfo) }
                    .doOnCancel { sessionManager.removeSessionInfo(sessionInfo) }
            }
    }

    private fun createSession(): Mono<SessionInfoDTO> {
        //TODO
        return getSessionInfo()
            .map { sessionInfo ->
                sessionInfo.copy(sessionId = UUID.randomUUID().toString())
            }
            .switchIfEmpty(Mono.fromSupplier { SessionInfoDTO("PANEL", "PANEL", "PANEL") })
    }

    private fun <T : Any> createHeartbeat(): ServerSentEvent<T> {
        return ServerSentEvent.builder<T>().comment("heartbeat").build()
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