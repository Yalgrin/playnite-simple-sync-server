package pl.yalgrin.playnite.simplesync.web.exception

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import pl.yalgrin.playnite.simplesync.dto.ErrorDTO
import pl.yalgrin.playnite.simplesync.exception.AuthException
import pl.yalgrin.playnite.simplesync.util.toErrorDTO
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper

@Order(-2)
@Component
class GlobalWebExceptionHandler(
    private val jsonMapper: JsonMapper
) : WebExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalWebExceptionHandler::class.java)
    }

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        if (ex is AuthException) {
            log.error("Authorization error occurred!", ex)
            return Mono.just(
                ex.toErrorDTO()
            )
                .flatMap { dto -> writeAsBytes(dto) }
                .map { exchange.response.bufferFactory().wrap(it) }
                .flatMap {
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    exchange.response.writeWith(Mono.just(it))
                }
                .onErrorResume { Mono.error(ex) }
        }

        return Mono.error(ex)
    }

    private fun writeAsBytes(dto: ErrorDTO): Mono<ByteArray> =
        Mono.fromCallable { jsonMapper.writeValueAsBytes(dto) }
}