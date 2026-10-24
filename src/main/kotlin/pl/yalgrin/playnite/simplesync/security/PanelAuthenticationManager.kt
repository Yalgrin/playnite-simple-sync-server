package pl.yalgrin.playnite.simplesync.security

import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AbstractUserDetailsReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.panel.service.PanelUserService
import reactor.core.publisher.Mono


@Primary
@Component
class PanelAuthenticationManager(
    private val panelUserService: PanelUserService,
    encoder: PasswordEncoder
) : AbstractUserDetailsReactiveAuthenticationManager() {
    init {
        setPasswordEncoder(encoder)
    }

    override fun retrieveUser(username: String): Mono<UserDetails> {
        return panelUserService.findByUsername(username)
    }

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return super.authenticate(authentication)
            .flatMap { authentication ->
                panelUserService.updateLoginTime(authentication.principal as UserDetails)
                    .thenReturn(authentication)
            }
    }
}