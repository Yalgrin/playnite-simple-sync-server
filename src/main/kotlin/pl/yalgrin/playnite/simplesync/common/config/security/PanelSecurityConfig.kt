package pl.yalgrin.playnite.simplesync.common.config.security

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.session.CookieWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver
import pl.yalgrin.playnite.simplesync.common.config.REMEMBER_ME_ATTRIBUTE_NAME
import pl.yalgrin.playnite.simplesync.common.config.SESSION_DURATION
import pl.yalgrin.playnite.simplesync.common.config.buildJsonMapper
import pl.yalgrin.playnite.simplesync.common.util.toErrorDTO
import pl.yalgrin.playnite.simplesync.exception.SecurityException
import pl.yalgrin.playnite.simplesync.exception.SecurityExceptionType
import pl.yalgrin.playnite.simplesync.security.PanelAuthenticationManager
import pl.yalgrin.playnite.simplesync.security.PanelSessionFilter
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Configuration
@EnableWebFluxSecurity
class PanelSecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityContextRepository(): ServerSecurityContextRepository =
        WebSessionServerSecurityContextRepository()

    @Bean
    @Order(0)
    fun security(
        http: ServerHttpSecurity,
        contexts: ServerSecurityContextRepository,
        panelAuthenticationManager: PanelAuthenticationManager
    ): SecurityWebFilterChain =
        http
            .securityMatcher(
                ServerWebExchangeMatchers
                    .pathMatchers("/api/panel/**")
            )
            .securityContextRepository(contexts)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .authenticationManager(panelAuthenticationManager)
            .exceptionHandling {
                it.authenticationEntryPoint(CustomServerAuthenticationEntryPoint())
                    .accessDeniedHandler(CustomServerAccessDeniedHandler())
            }
            .authorizeExchange {
                it.pathMatchers(HttpMethod.GET, "/api/panel/auth/csrf").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/panel/auth/login").permitAll()
                    .anyExchange().authenticated()
            }
            .csrf(Customizer.withDefaults())
            .addFilterAfter(PanelSessionFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .build()

    @Bean
    fun webSessionIdResolver(): WebSessionIdResolver = OptionalRememberCookieWebSessionIdResolver()
}

class OptionalRememberCookieWebSessionIdResolver(
    val sessionResolver: CookieWebSessionIdResolver = CookieWebSessionIdResolver(),
    val rememberedResolver: CookieWebSessionIdResolver = CookieWebSessionIdResolver().apply {
        cookieMaxAge = SESSION_DURATION
    }
) : WebSessionIdResolver by sessionResolver {

    override fun setSessionId(exchange: ServerWebExchange, sessionId: String) {
        val resolver = if (exchange.getAttribute<Boolean>(REMEMBER_ME_ATTRIBUTE_NAME) == true) {
            rememberedResolver
        } else {
            sessionResolver
        }
        resolver.setSessionId(exchange, sessionId)
    }
}

class CustomServerAuthenticationEntryPoint : ServerAuthenticationEntryPoint {
    companion object {
        private val log = LoggerFactory.getLogger(CustomServerAuthenticationEntryPoint::class.java)
    }

    private val jsonMapper = buildJsonMapper()

    override fun commence(
        exchange: ServerWebExchange,
        ex: AuthenticationException
    ): Mono<Void> {
        return Mono.fromSupplier {
            log.warn("Unauthorized access attempt", ex)
            SecurityException(SecurityExceptionType.UNAUTHORIZED)
        }
            .map { it.toErrorDTO() }
            .flatMap { errorDTO ->
                Mono.fromCallable { jsonMapper.writeValueAsBytes(errorDTO) }
                    .subscribeOn(Schedulers.parallel())
            }
            .flatMap { bytes ->
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                exchange.response.writeWith(Mono.just(exchange.response.bufferFactory().wrap(bytes)))
            }
    }
}

class CustomServerAccessDeniedHandler : ServerAccessDeniedHandler {
    companion object {
        private val log = LoggerFactory.getLogger(CustomServerAuthenticationEntryPoint::class.java)
    }

    private val jsonMapper = buildJsonMapper()

    override fun handle(
        exchange: ServerWebExchange,
        ex: AccessDeniedException
    ): Mono<Void> {
        return Mono.fromSupplier {
            log.warn("Forbidden access attempt", ex)
            SecurityException(SecurityExceptionType.FORBIDDEN)
        }
            .map { it.toErrorDTO() }
            .flatMap { errorDTO ->
                Mono.fromCallable { jsonMapper.writeValueAsBytes(errorDTO) }
                    .subscribeOn(Schedulers.parallel())
            }
            .flatMap { bytes ->
                exchange.response.statusCode = HttpStatus.FORBIDDEN
                exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                exchange.response.writeWith(Mono.just(exchange.response.bufferFactory().wrap(bytes)))
            }
    }

}