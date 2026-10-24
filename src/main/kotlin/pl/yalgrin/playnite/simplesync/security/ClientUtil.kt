package pl.yalgrin.playnite.simplesync.security

import org.springframework.security.core.context.ReactiveSecurityContextHolder
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.panel.service.PanelUserDetails
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

fun getSessionInfo(): Mono<SessionInfoDTO> {
    return ReactiveSecurityContextHolder.getContext()
        .flatMap { context ->
            val authentication = context.authentication
            if (authentication is ClientPrincipal) {
                Mono.just(authentication.sessionInfo)
            } else {
                val principal = authentication?.principal
                if (principal is PanelUserDetails) {
                    principal.toMono()
                        .flatMap { u ->
                            getPanelSessionId()
                                .map { sessionId ->
                                    SessionInfoDTO(
                                        u.id?.toString() ?: "",
                                        u.username,
                                        sessionId,
                                        true
                                    )
                                }
                        }
                } else {
                    Mono.empty()
                }
            }
        }
}

private fun getPanelSessionId(): Mono<String> {
    return Mono.deferContextual { contextView ->
        Mono.fromSupplier { contextView.get<String>("X-Session-Id") }
    }.defaultIfEmpty("")
}

fun getSessionClientId(): Mono<String> {
    return ReactiveSecurityContextHolder.getContext()
        .flatMap { context ->
            val authentication = context.authentication
            if (authentication is ClientPrincipal) {
                Mono.just(authentication.sessionInfo)
                    .map { it.clientId }
            } else {
                val principal = authentication?.principal
                if (principal is PanelUserDetails) {
                    principal.toMono()
                        .map { it.id?.toString() ?: "" }
                } else {
                    Mono.empty()
                }
            }
        }
}

fun getPanelUser(): Mono<PanelUserDetails> {
    return ReactiveSecurityContextHolder.getContext()
        .flatMap { context ->
            val principal = context.authentication?.principal
            if (principal is PanelUserDetails) {
                principal.toMono()
            } else {
                Mono.empty()
            }
        }
}