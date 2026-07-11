package pl.yalgrin.playnite.simplesync.exception

import java.io.Serial

class ApiVersionException(val type: ApiVersionExceptionType) : RuntimeException() {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 8801390324945729858L
    }
}