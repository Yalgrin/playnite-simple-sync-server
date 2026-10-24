package pl.yalgrin.playnite.simplesync.security

import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class PanelSessionFilter : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        return Mono.fromSupplier { exchange.response.headers }
            .mapNotNull { headers -> headers.get("X-Session-Id")?.firstOrNull() }
            .defaultIfEmpty("")
            .flatMap { str ->
                chain.filter(exchange)
                    .contextWrite { it.put("X-Session-Id", str) }
            }
    }
}