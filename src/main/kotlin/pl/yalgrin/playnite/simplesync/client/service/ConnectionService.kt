package pl.yalgrin.playnite.simplesync.client.service

import org.slf4j.LoggerFactory
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.message.ConnectionMessage
import pl.yalgrin.playnite.simplesync.client.message.InitializationMessage
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
                    Flux.interval(Duration.ofSeconds(30)).map { createHeartbeat() }
                )
                    .doOnSubscribe { sessionManager.saveSessionInfo(sessionInfo) }
                    .doOnTerminate { sessionManager.removeSessionInfo(sessionInfo) }
                    .doOnCancel { sessionManager.removeSessionInfo(sessionInfo) }
            }
            .doOnSubscribe { log.debug("Connection established") }
            .doOnNext { log.debug("Connection next") }
            .doOnCancel { log.debug("Connection cancelled") }
            .doOnTerminate { log.debug("Connection terminated") }
            .doOnEach { s -> log.debug("Connection signal {}", s.type) }
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
}