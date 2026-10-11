package pl.yalgrin.playnite.simplesync.common.util

import pl.yalgrin.playnite.simplesync.dto.ErrorDTO
import pl.yalgrin.playnite.simplesync.exception.ApiVersionException
import pl.yalgrin.playnite.simplesync.exception.AuthException
import pl.yalgrin.playnite.simplesync.exception.NotFoundException
import pl.yalgrin.playnite.simplesync.exception.ValidationException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

fun AuthException.toErrorDTO(): ErrorDTO {
    return ErrorDTO(message = "AuthException.${this.type.name}")
}

fun ApiVersionException.toErrorDTO(): ErrorDTO {
    return ErrorDTO(message = "ApiVersionException.${this.type.name}")
}

fun ValidationException.toErrorDTO(): ErrorDTO {
    return ErrorDTO(
        message = "ValidationException",
        fieldErrors = this.errors
    )
}

fun <T : Any> Mono<T>.switchToNotFoundIfEmpty(): Mono<T> = this.switchIfEmpty {
    Mono.error(NotFoundException())
}