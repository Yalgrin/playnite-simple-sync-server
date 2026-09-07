package pl.yalgrin.playnite.simplesync.security

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.client.helper.RegistrationHandler
import reactor.core.publisher.Mono

@Component
class ClientReactiveAuthenticationManager(
    private val registrationHandler: RegistrationHandler
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.fromSupplier {
            authentication as? ClientAuthenticationToken
        }
            .flatMap { token ->
                registrationHandler.getSessionInfo(token)
            }.map { ClientPrincipal(it) }
    }
}
