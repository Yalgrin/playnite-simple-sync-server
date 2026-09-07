package pl.yalgrin.playnite.simplesync.client.helper

import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationInfoDTO
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.repository.RegisteredClientRepository
import pl.yalgrin.playnite.simplesync.common.util.pairWith
import pl.yalgrin.playnite.simplesync.common.util.toSha1
import pl.yalgrin.playnite.simplesync.exception.AuthException
import pl.yalgrin.playnite.simplesync.exception.AuthExceptionType
import pl.yalgrin.playnite.simplesync.security.ClientAuthenticationToken
import pl.yalgrin.playnite.simplesync.security.SessionManager
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Component
class RegistrationHandler(
    val registeredClientRepository: RegisteredClientRepository,
    val sessionManager: SessionManager,
) {
    fun getSessionInfo(token: ClientAuthenticationToken): Mono<SessionInfoDTO> {
        return doGetSessionInfo(token)
            .filter { it.clientId.isNotBlank() && it.displayName.isNotBlank() && (token.allowBlankSessionId || it.sessionId.isNotBlank()) }
            .switchIfEmpty(Mono.error(AuthException(AuthExceptionType.NO_VALID_CLIENT_SESSION)))
    }

    fun getRegistrationInfo(exchange: ServerWebExchange): Mono<RegistrationInfoDTO> {
        return fetchHeaders(exchange.request.headers)
            .flatMap { (clientId, clientToken, sessionId) ->
                val validRegistration = registeredClientRepository.findByClientId(clientId)
                    .map {
                        if (it.clientToken == clientToken.toSha1()) {
                            it.displayName
                        } else {
                            ""
                        }
                    }
                    .defaultIfEmpty("")
                val sessionActive = sessionId.toMono()
                    .filter { it.isNotBlank() }
                    .flatMap { sessionManager.getSessionInfoMono(it) }
                    .map { it.clientId == clientId }
                    .defaultIfEmpty(false)

                validRegistration.pairWith(sessionActive)
                    .map { (validRegistration, sessionActive) ->
                        RegistrationInfoDTO(
                            registrationSpecified = clientId.isNotBlank() && clientToken.isNotBlank(),
                            registrationValid = validRegistration.isNotBlank(),
                            displayClientName = validRegistration,
                            sessionActive = sessionActive
                        )
                    }
            }
    }

    private fun doGetSessionInfo(token: ClientAuthenticationToken): Mono<SessionInfoDTO> = fetchHeaders(token)
        .flatMap { (clientId, clientToken, sessionId) ->
            if (clientId.isBlank() || clientToken.isBlank()) {
                Mono.error(AuthException(AuthExceptionType.MISSING_REGISTRATION))
            } else {
                Mono.zip(
                    registeredClientRepository.findByClientId(clientId)
                        .filter { client ->
                            client.clientToken == clientToken.toSha1()
                        }
                        .switchIfEmpty(Mono.error(AuthException(AuthExceptionType.INVALID_REGISTRATION))),
                    sessionManager.getSessionInfoMono(sessionId)
                        .filter { it.clientId == clientId }
                        .map { Optional.of(it) }
                        .defaultIfEmpty(Optional.empty())
                )
                    .map { Pair(it.t1, it.t2) }
                    .map { (client, sessionInfo) ->
                        SessionInfoDTO(
                            clientId = clientId,
                            displayName = client.displayName,
                            sessionId = sessionInfo.map { it.sessionId }.orElse(null) ?: ""
                        )
                    }
            }
        }

    private fun fetchHeaders(headers: HttpHeaders): Mono<Triple<String, String, String>> = Mono.zip(
        Mono.fromSupplier { headers.getFirst("X-Client-Id") ?: "" },
        Mono.fromSupplier { headers.getFirst("X-Client-Token") ?: "" },
        Mono.fromSupplier { headers.getFirst("X-Session-Id") ?: "" }
    )
        .map { Triple(it.t1, it.t2, it.t3) }

    private fun fetchHeaders(token: ClientAuthenticationToken): Mono<Triple<String, String, String>> =
        Mono.fromSupplier {
            Triple(token.clientId ?: "", token.clientToken ?: "", token.sessionId ?: "")
        }
}