package pl.yalgrin.playnite.simplesync.client.filter

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.helper.RegistrationHandler
import pl.yalgrin.playnite.simplesync.security.withSessionInfo
import reactor.core.publisher.Mono

@Component
class RegistrationFilter(
    val registrationHandler: RegistrationHandler
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        return if (shouldNotRequireSessionOnUrl(exchange.request.uri.path)) {
            chain.filter(exchange)
        } else if (shouldAllowSessionWithoutSessionId(exchange.request.uri.path)) {
            registrationHandler.getSessionInfoWithoutSessionId(exchange.request.headers)
                .flatMap { sessionInfo ->
                    processWithSession(chain, exchange, sessionInfo)
                }
        } else {
            registrationHandler.getSessionInfo(exchange.request.headers)
                .flatMap { sessionInfo ->
                    processWithSession(chain, exchange, sessionInfo)
                }
        }
    }

    private fun shouldNotRequireSessionOnUrl(url: String): Boolean {
        return url == "/api/client/register" || url == "/api/health" || url == "/api/client/check"
    }

    private fun shouldAllowSessionWithoutSessionId(url: String): Boolean {
        return url == "/api/client/connect" || url == "/api/change" || url == "/api/client/change-name"
    }

    private fun processWithSession(
        chain: WebFilterChain,
        exchange: ServerWebExchange,
        sessionInfo: SessionInfoDTO
    ): Mono<Void> = chain.filter(exchange)
        .contextWrite(withSessionInfo(sessionInfo))
}