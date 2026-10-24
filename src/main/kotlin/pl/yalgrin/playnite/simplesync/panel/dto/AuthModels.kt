package pl.yalgrin.playnite.simplesync.panel.dto

import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil

data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean
) {
    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("username", username)
            .append("rememberMe", rememberMe)
            .build()
    }
}

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
) {
    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .build()
    }
}

data class UserResponse(val username: String)

data class CsrfResponse(val headerName: String, val token: String) {
    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("headerName", headerName)
            .build()
    }
}