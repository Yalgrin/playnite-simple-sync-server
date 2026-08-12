package pl.yalgrin.playnite.simplesync.exception

import java.io.Serial

class ForceFetchRequiredException(override val message: String) : RuntimeException(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -243428804386672971L
    }
}