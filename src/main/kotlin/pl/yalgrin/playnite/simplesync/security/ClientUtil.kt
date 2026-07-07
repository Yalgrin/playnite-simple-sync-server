package pl.yalgrin.playnite.simplesync.security

import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import reactor.core.publisher.Mono
import reactor.util.context.Context

private const val SESSION_KEY = "simple-sync-session"

fun getSessionInfo(): Mono<SessionInfoDTO> {
    return Mono.deferContextual { context ->
        Mono.fromSupplier {
            context.getOrDefault<SessionInfoDTO>(SESSION_KEY, null)
        }
    }
}

fun getSessionClientId(): Mono<String> {
    return getSessionInfo()
        .map { it.clientId }
}

fun withSessionInfo(sessionInfo: SessionInfoDTO): Context {
    return Context.of(SESSION_KEY, sessionInfo)
}