package pl.yalgrin.playnite.simplesync.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.util.AntPathMatcher
import org.springframework.util.PathMatcher
import org.springframework.web.server.ServerWebExchange
import pl.yalgrin.playnite.simplesync.security.ClientAuthenticationEntryPoint
import pl.yalgrin.playnite.simplesync.security.ClientAuthenticationToken
import pl.yalgrin.playnite.simplesync.security.ClientReactiveAuthenticationManager
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper

private val PUBLIC_PATHS = listOf(
    "/api/health",
    "/api/client/register",
    "/api/client/check"
)

private val LENIENT_PATHS = listOf(
    "/api/client/connect",
    "/api/client/change-name",
    "/api/change/**"
)

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(
    private val jsonMapper: JsonMapper
) {
    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        authManager: ClientReactiveAuthenticationManager
    ): SecurityWebFilterChain {
        val authFilter = AuthenticationWebFilter(authManager)
        authFilter.setServerAuthenticationConverter(this::toAuthenticationToken)
        authFilter.setRequiresAuthenticationMatcher(protectedPathsMatcher())

        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(*PUBLIC_PATHS.toTypedArray()).permitAll()
                    .pathMatchers("/api/**").authenticated()
                    .anyExchange().permitAll()
            }
            .exceptionHandling { handling ->
                handling.authenticationEntryPoint(ClientAuthenticationEntryPoint(jsonMapper))
            }
            .build()
    }

    private fun toAuthenticationToken(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.fromSupplier {
            exchange.request.path.value()
        }.map { path ->
            val headers = exchange.request.headers
            val allowBlankSessionId = LENIENT_PATHS.any { pathMatches(it, path) }
            ClientAuthenticationToken(
                clientId = headers.getFirst("X-Client-Id"),
                clientToken = headers.getFirst("X-Client-Token"),
                sessionId = headers.getFirst("X-Session-Id"),
                allowBlankSessionId = allowBlankSessionId
            )
        }
    }

    private fun protectedPathsMatcher(): ServerWebExchangeMatcher {
        return ServerWebExchangeMatcher { exchange ->
            val path = exchange.request.path.value()
            if (path.startsWith("/api/") && PUBLIC_PATHS.none { pathMatches(it, path) }) {
                ServerWebExchangeMatcher.MatchResult.match()
            } else {
                ServerWebExchangeMatcher.MatchResult.notMatch()
            }
        }
    }

    private val pathMatcher: PathMatcher = AntPathMatcher()

    private fun pathMatches(pattern: String, path: String): Boolean {
        return pathMatcher.match(pattern, path)
    }
}