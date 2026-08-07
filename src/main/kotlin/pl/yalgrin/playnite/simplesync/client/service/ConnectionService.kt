package pl.yalgrin.playnite.simplesync.client.service

import org.slf4j.LoggerFactory
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.client.message.ConnectionMessage
import pl.yalgrin.playnite.simplesync.client.message.InitializationMessage
import pl.yalgrin.playnite.simplesync.security.SessionManager
import pl.yalgrin.playnite.simplesync.security.getSessionInfo
import pl.yalgrin.playnite.simplesync.util.thenAny
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Service
class ConnectionService(
    private val sessionManager: SessionManager,
    private val registeredClientService: RegisteredClientService,
    private val changeListenerService: ChangeListenerService
) {
    companion object {
        private val log = LoggerFactory.getLogger(ConnectionService::class.java)
    }

    fun connect(): Flux<ServerSentEvent<ConnectionMessage>> {
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
                            .filter { it.isForceFetch || it.clientId != sessionInfo.clientId }.filterWhen {
                            sessionManager.getSessionSettingsMono(sessionInfo.sessionId).map { it.enabledChangeStream }
                                .defaultIfEmpty(false)
                        }.map {
                            ServerSentEvent.builder<ConnectionMessage>().data(
                                ChangeMessage(
                                    id = it.id,
                                    type = it.type,
                                    clientId = it.clientId,
                                    objectId = it.objectId,
                                    forceFetch = it.isForceFetch
                                )
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
        return getSessionInfo()
            .map { sessionInfo ->
                sessionInfo.copy(sessionId = UUID.randomUUID().toString())
            }
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