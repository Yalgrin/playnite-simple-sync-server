package pl.yalgrin.playnite.simplesync.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReactorUtils {
    public static <T> Mono<T> toMono(Flux<T> flux) {
        return Mono.from(flux.take(1));
    }
}
