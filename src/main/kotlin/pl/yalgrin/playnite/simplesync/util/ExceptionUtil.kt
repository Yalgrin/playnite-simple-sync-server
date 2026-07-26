package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.dto.ErrorDTO
import pl.yalgrin.playnite.simplesync.exception.ApiVersionException
import pl.yalgrin.playnite.simplesync.exception.AuthException

fun AuthException.toErrorDTO(): ErrorDTO {
    return ErrorDTO("AuthException.${this.type.name}")
}

fun ApiVersionException.toErrorDTO(): ErrorDTO {
    return ErrorDTO("ApiVersionException.${this.type.name}")
}