package pl.yalgrin.playnite.simplesync.exception

import java.io.Serial

class ManualSynchronizationRequiredException(override val message: String) : RuntimeException(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -8664623010021064227L
    }
}