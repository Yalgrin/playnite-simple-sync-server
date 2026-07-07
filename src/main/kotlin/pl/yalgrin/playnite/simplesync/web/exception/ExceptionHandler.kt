package pl.yalgrin.playnite.simplesync.web.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.yalgrin.playnite.simplesync.exception.AuthException
import reactor.core.publisher.Mono

@RestControllerAdvice
class ExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ExceptionHandler::class.java)
    }

    //TODO
    @ExceptionHandler(AuthException::class)
    fun handleAuthException(ex: AuthException): Mono<ResponseEntity<String>> {
        log.error("Authorization error occurred!", ex)
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("An error occurred: ${ex.message}"))
    }
}