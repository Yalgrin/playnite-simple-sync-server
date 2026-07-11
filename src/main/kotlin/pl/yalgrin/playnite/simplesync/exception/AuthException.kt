package pl.yalgrin.playnite.simplesync.exception

import java.io.Serial

class AuthException(val type: AuthExceptionType) : RuntimeException() {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -4500434712346055235L
    }
}