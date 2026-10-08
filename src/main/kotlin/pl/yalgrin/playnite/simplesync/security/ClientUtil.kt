package pl.yalgrin.playnite.simplesync.security

import org.springframework.security.core.context.ReactiveSecurityContextHolder
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.common.util.defaultIfEmpty
import reactor.core.publisher.Mono

fun getSessionInfo(): Mono<SessionInfoDTO> {
    return ReactiveSecurityContextHolder.getContext()
        .flatMap { context ->
            val authentication = context.authentication
            if (authentication is ClientPrincipal) {
                Mono.just(authentication.sessionInfo)
            } else {
                Mono.empty()
            }
        }
}

fun getSessionClientId(): Mono<String> {
    //TODO
    return getSessionInfo()
        .map { it.clientId }
        .defaultIfEmpty { "empty" }
}
