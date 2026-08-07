package pl.yalgrin.playnite.simplesync.web.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/health")
class HealthResource {
    @GetMapping
    fun testConnection(): Mono<String> {
        return Mono.just("OK")
    }
}
