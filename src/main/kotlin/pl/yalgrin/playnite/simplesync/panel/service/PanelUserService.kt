package pl.yalgrin.playnite.simplesync.panel.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.common.helper.ITransactional
import pl.yalgrin.playnite.simplesync.panel.domain.PanelUser
import pl.yalgrin.playnite.simplesync.panel.dto.ChangePasswordRequest
import pl.yalgrin.playnite.simplesync.panel.repository.PanelUserRepository
import reactor.core.publisher.Mono
import java.io.Serial
import java.time.Instant
import java.util.*

class PanelUserDetails(
    val id: UUID?,
    username: String,
    password: String,
) : User(username, password, listOf(SimpleGrantedAuthority("ROLE_PANEL_USER"))) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 6988684902479846001L
    }
}

@Service
class PanelUserService(
    private val panelUserRepository: PanelUserRepository,
    private val passwordEncoder: PasswordEncoder,
    override val reactiveTransactionManager: ReactiveTransactionManager
) : ITransactional, ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {
    companion object {
        private val log = LoggerFactory.getLogger(PanelUserService::class.java)
    }

    init {
        init().block()
    }

    fun init(): Mono<PanelUser> = panelUserRepository.existsAny()
        .defaultIfEmpty(false)
        .flatMap { anyThingExists ->
            if (!anyThingExists) {
                log.info("No users found in the database. Creating a default admin user.")
                panelUserRepository.save(PanelUser(username = "admin", password = passwordEncoder.encode("admin")!!))
            } else {
                Mono.empty()
            }
        }

    override fun findByUsername(username: String): Mono<UserDetails> {
        return panelUserRepository.findByUsername(username)
            .map { PanelUserDetails(it.id, it.username, it.password) }
            .transactionalReadOnly()
            .cast(UserDetails::class.java)
    }

    override fun updatePassword(
        user: UserDetails,
        newPassword: String?
    ): Mono<UserDetails> {
        if (newPassword == null) {
            return Mono.just(user)
        }
        return panelUserRepository.findByUsername(user.username)
            .map { it.copy(password = newPassword) }
            .flatMap(panelUserRepository::save)
            .map { PanelUserDetails(it.id, it.username, it.password) }
            .transactional()
            .cast(UserDetails::class.java)
    }

    fun changePassword(
        user: UserDetails,
        passwordRequest: ChangePasswordRequest
    ): Mono<PanelUserDetails> {
        return panelUserRepository.findByUsername(user.username)
            .filterWhen {
                Mono.fromSupplier { passwordEncoder.matches(passwordRequest.oldPassword, it.password) }
                    .defaultIfEmpty(false)
                    .flatMap { matches ->
                        if (matches) {
                            Mono.just(true)
                        } else {
                            Mono.error(RuntimeException("Invalid password"))
                        }
                    }
            }
            .map {
                val newPassword = passwordEncoder.encode(passwordRequest.newPassword)
                it.copy(password = newPassword!!)
            }
            .flatMap(panelUserRepository::save)
            .map { PanelUserDetails(it.id, it.username, it.password) }
            .transactional()
    }

    fun updateLoginTime(user: UserDetails): Mono<PanelUserDetails> {
        return panelUserRepository.findByUsername(user.username)
            .map { it.copy(lastLogin = Instant.now()) }
            .flatMap(panelUserRepository::save)
            .transactional()
            .map { PanelUserDetails(it.id, it.username, it.password) }
    }

}