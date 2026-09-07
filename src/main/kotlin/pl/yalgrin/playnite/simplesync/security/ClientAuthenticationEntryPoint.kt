package pl.yalgrin.playnite.simplesync.security

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.web.server.ServerWebExchange
import pl.yalgrin.playnite.simplesync.common.util.toErrorDTO
import pl.yalgrin.playnite.simplesync.exception.AuthException
import pl.yalgrin.playnite.simplesync.exception.AuthExceptionType
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import tools.jackson.databind.json.JsonMapper

class ClientAuthenticationEntryPoint(
    private val jsonMapper: JsonMapper
) : ServerAuthenticationEntryPoint {
    override fun commence(
        exchange: ServerWebExchange,
        ex: AuthenticationException
    ): Mono<Void> {
        return Mono.fromSupplier { AuthException(AuthExceptionType.NO_VALID_CLIENT_SESSION) }
            .map { it.toErrorDTO() }
            .flatMap { errorDTO ->
                Mono.fromCallable { jsonMapper.writeValueAsBytes(errorDTO) }
                    .subscribeOn(Schedulers.parallel())
            }
            .flatMap { bytes ->
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                exchange.response.writeWith(Mono.just(exchange.response.bufferFactory().wrap(bytes)))
            }
    }
}