package pl.yalgrin.playnite.simplesync.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import java.io.Serial
import java.security.Principal

class ClientPrincipal(
    val sessionInfo: SessionInfoDTO
) : Authentication, Principal {
    override fun getAuthorities(): List<SimpleGrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_PLUGIN_USER"))

    override fun getCredentials(): Any? = null

    override fun getDetails(): Any? = null

    override fun getPrincipal(): Any = this

    override fun isAuthenticated(): Boolean = true

    override fun setAuthenticated(isAuthenticated: Boolean) {
        throw UnsupportedOperationException("Cannot change authentication status")
    }

    override fun getName(): String = sessionInfo.displayName

    companion object {
        @Serial
        private const val serialVersionUID: Long = -5500831677951572747L
    }
}
