package pl.yalgrin.playnite.simplesync.exception

import java.io.Serial

class NotFoundException : RuntimeException() {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -4299726454920578172L
    }
}