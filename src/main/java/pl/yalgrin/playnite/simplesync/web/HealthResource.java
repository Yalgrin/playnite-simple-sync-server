package pl.yalgrin.playnite.simplesync.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/health")
public class HealthResource {
    @GetMapping
    public Mono<String> testConnection() {
        return Mono.just("OK");
    }
}
