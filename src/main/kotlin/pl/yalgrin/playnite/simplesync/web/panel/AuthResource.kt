package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import pl.yalgrin.playnite.simplesync.common.config.SESSION_DURATION
import pl.yalgrin.playnite.simplesync.panel.dto.ChangePasswordRequest
import pl.yalgrin.playnite.simplesync.panel.dto.CsrfResponse
import pl.yalgrin.playnite.simplesync.panel.dto.LoginRequest
import pl.yalgrin.playnite.simplesync.panel.dto.UserResponse
import pl.yalgrin.playnite.simplesync.panel.service.PanelUserService
import pl.yalgrin.playnite.simplesync.security.PanelAuthenticationManager
import pl.yalgrin.playnite.simplesync.security.getPanelUser
import reactor.core.publisher.Mono
import java.time.Duration


@RestController
@RequestMapping("/api/panel/auth")
class AuthResource(
    private val userService: PanelUserService,
    private val panelAuthenticationManager: PanelAuthenticationManager,
    private val contexts: ServerSecurityContextRepository
) {

    @GetMapping("/csrf")
    fun getCsrf(exchange: ServerWebExchange): Mono<CsrfResponse> {
        return getCsrfToken(exchange).map { CsrfResponse(it.headerName, it.token) }
    }

    private fun getCsrfToken(exchange: ServerWebExchange): Mono<CsrfToken> =
        exchange.getAttribute(CsrfToken::class.java.name)
            ?: Mono.error(
                IllegalStateException("CSRF token is unavailable")
            )

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        exchange: ServerWebExchange
    ): Mono<UserResponse> =
        userService.findByUsername(request.username)
            .switchIfEmpty(Mono.error(BadCredentialsException("Failed login!")))
            .map {
                UsernamePasswordAuthenticationToken.unauthenticated(
                    it,
                    request.password
                )
            }
            .flatMap { panelAuthenticationManager.authenticate(it) }
            .flatMap { authentication ->
                exchange.attributes["panel.rememberMe"] = request.rememberMe
                exchange.session.flatMap { session ->
                    session.maxIdleTime =
                        if (request.rememberMe) SESSION_DURATION else Duration.ofHours(1)
                    session.changeSessionId()
                        .then(
                            contexts.save(
                                exchange,
                                SecurityContextImpl(authentication)
                            )
                        )
                        .thenReturn(UserResponse(authentication.name))
                }
            }

    @GetMapping("/me")
    fun getCurrentUser(): Mono<UserResponse> =
        getPanelUser().map { userDetails -> UserResponse(userDetails.username) }

    @PostMapping("/change-password")
    fun changePassword(@RequestBody request: ChangePasswordRequest): Mono<UserResponse> {
        return getPanelUser()
            .flatMap { userService.changePassword(it, request) }
            .map { UserResponse(it.username) }
    }

    @PostMapping("/logout")
    fun logout(exchange: ServerWebExchange): Mono<Void> =
        exchange.session.flatMap { session ->
            session.invalidate()
        }
}