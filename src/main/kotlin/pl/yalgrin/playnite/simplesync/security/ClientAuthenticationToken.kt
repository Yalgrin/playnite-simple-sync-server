package pl.yalgrin.playnite.simplesync.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import java.io.Serial

class ClientAuthenticationToken(
    val clientId: String?,
    val clientToken: String?,
    val sessionId: String?,
    val allowBlankSessionId: Boolean
) : Authentication {

    override fun getAuthorities(): List<GrantedAuthority> = listOf()

    override fun getCredentials(): Any? = null

    override fun getDetails(): Any? = null

    override fun getPrincipal(): Any? = null

    override fun isAuthenticated(): Boolean = false

    override fun setAuthenticated(isAuthenticated: Boolean) {
        throw UnsupportedOperationException("Cannot change authentication status")
    }

    override fun getName(): String? = null

    companion object {
        @Serial
        private const val serialVersionUID: Long = 6030016599039941925L
    }
}
