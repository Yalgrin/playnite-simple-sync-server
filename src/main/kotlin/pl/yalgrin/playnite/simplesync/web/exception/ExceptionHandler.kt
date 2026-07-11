package pl.yalgrin.playnite.simplesync.web.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.yalgrin.playnite.simplesync.dto.ErrorDTO
import pl.yalgrin.playnite.simplesync.exception.ApiVersionException
import pl.yalgrin.playnite.simplesync.exception.AuthException
import pl.yalgrin.playnite.simplesync.exception.ForceFetchRequiredException
import pl.yalgrin.playnite.simplesync.exception.ManualSynchronizationRequiredException
import reactor.core.publisher.Mono

@RestControllerAdvice
class ExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ExceptionHandler::class.java)
    }

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(ex: AuthException): Mono<ResponseEntity<ErrorDTO>> {
        log.error("Authorization error occurred!", ex)
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorDTO("AuthException.${ex.type.name}")))
    }

    @ExceptionHandler(ApiVersionException::class)
    fun handleApiVersionException(ex: ApiVersionException): Mono<ResponseEntity<ErrorDTO>> {
        log.error("API version error occurred!", ex)
        return Mono.just(
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorDTO("ApiVersionException.${ex.type.name}"))
        )
    }

    @ExceptionHandler(ManualSynchronizationRequiredException::class)
    fun handleManualSynchronizationRequiredException(ex: ManualSynchronizationRequiredException): Mono<ResponseEntity<ErrorDTO>> {
        log.error("Manual synchronization required!", ex)
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorDTO("manualSyncRequired")))
    }

    @ExceptionHandler(ForceFetchRequiredException::class)
    fun handleForceFetchRequiredException(ex: ForceFetchRequiredException): Mono<ResponseEntity<ErrorDTO>> {
        log.error("Force fetch required!", ex)
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorDTO("forceFetchRequired")))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): Mono<ResponseEntity<ErrorDTO>> {
        log.error("Unknown exception!", ex)
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorDTO("exception")))
    }
}