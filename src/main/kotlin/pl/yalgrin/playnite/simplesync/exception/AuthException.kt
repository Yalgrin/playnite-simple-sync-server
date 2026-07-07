package pl.yalgrin.playnite.simplesync.exception

import java.io.Serial

class AuthException : RuntimeException() {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -4500434712346055235L
    }
}