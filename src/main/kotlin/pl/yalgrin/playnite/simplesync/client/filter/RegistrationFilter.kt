package pl.yalgrin.playnite.simplesync.client.filter

import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.repository.RegisteredClientRepository
import pl.yalgrin.playnite.simplesync.exception.AuthException
import pl.yalgrin.playnite.simplesync.exception.AuthExceptionType
import pl.yalgrin.playnite.simplesync.security.SessionManager
import pl.yalgrin.playnite.simplesync.security.withSessionInfo
import pl.yalgrin.playnite.simplesync.util.toSha1
import reactor.core.publisher.Mono
import java.util.*

@Component
class RegistrationFilter(
    val registeredClientRepository: RegisteredClientRepository,
    val sessionManager: SessionManager
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        return if (shouldNotRequireSessionOnUrl(exchange.request.uri.path)) {
            chain.filter(exchange)
        } else if (shouldAllowSessionWithoutSessionId(exchange.request.uri.path)) {
            getSessionInfoWithoutSessionId(exchange.request.headers)
                .flatMap { sessionInfo ->
                    processWithSession(chain, exchange, sessionInfo)
                }
        } else {
            getSessionInfo(exchange.request.headers)
                .flatMap { sessionInfo ->
                    processWithSession(chain, exchange, sessionInfo)
                }
        }
    }

    private fun shouldNotRequireSessionOnUrl(url: String): Boolean {
        return url == "/api/client/register" || url == "/api/health"
    }

    private fun shouldAllowSessionWithoutSessionId(url: String): Boolean {
        return url == "/api/client/connect" || url == "/api/client/check" || url == "/api/change" || url == "/api/client/change-name"
    }

    private fun getSessionInfoWithoutSessionId(headers: HttpHeaders): Mono<SessionInfoDTO> {
        return doGetSessionInfo(headers)
            .filter { it.clientId.isNotBlank() && it.displayName.isNotBlank() }
            .switchIfEmpty(Mono.error(AuthException(AuthExceptionType.NO_VALID_CLIENT_SESSION)))
    }

    private fun getSessionInfo(headers: HttpHeaders): Mono<SessionInfoDTO> {
        return doGetSessionInfo(headers)
            .filter { it.clientId.isNotBlank() && it.displayName.isNotBlank() && it.sessionId.isNotBlank() }
            .switchIfEmpty(Mono.error(AuthException(AuthExceptionType.NO_VALID_CLIENT_SESSION)))
    }

    private fun doGetSessionInfo(headers: HttpHeaders): Mono<SessionInfoDTO> = fetchHeaders(headers)
        .filter { it.first.isNotBlank() && it.second.isNotBlank() }
        .flatMap { (clientId, clientToken, sessionId) ->
            Mono.zip(
                registeredClientRepository.findByClientId(clientId)
                    .filter { it.clientToken == clientToken.toSha1() },
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

    private fun fetchHeaders(headers: HttpHeaders): Mono<Triple<String, String, String>> = Mono.zip(
        Mono.fromSupplier { headers.getFirst("X-Client-Id") ?: "" },
        Mono.fromSupplier { headers.getFirst("X-Client-Token") ?: "" },
        Mono.fromSupplier { headers.getFirst("X-Session-Id") ?: "" }
    )
        .map { Triple(it.t1, it.t2, it.t3) }

    private fun processWithSession(
        chain: WebFilterChain,
        exchange: ServerWebExchange,
        sessionInfo: SessionInfoDTO
    ): Mono<Void> = chain.filter(exchange)
        .contextWrite(withSessionInfo(sessionInfo))
}