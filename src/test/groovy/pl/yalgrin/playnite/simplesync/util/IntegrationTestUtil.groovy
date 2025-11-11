package pl.yalgrin.playnite.simplesync.util

import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class IntegrationTestUtil {
    static <T> Flux<T> getReturnFlux(WebTestClient.ResponseSpec response, Class<T> clazz) {
        return response.returnResult(clazz).responseBody
    }

    static <T> Mono<T> getReturnMono(WebTestClient.ResponseSpec response, Class<T> clazz) {
        return getReturnFlux(response, clazz).take(1).next()
    }
}
