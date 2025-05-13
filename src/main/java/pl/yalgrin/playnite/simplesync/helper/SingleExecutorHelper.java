package pl.yalgrin.playnite.simplesync.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class SingleExecutorHelper {
    private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();

    public <T> Mono<T> runOnExecutor(Mono<T> mono) {
        return Mono.fromCallable(() -> mono.subscribeOn(Schedulers.immediate()).block())
                .subscribeOn(Schedulers.fromExecutorService(asyncExecutor))
                .publishOn(Schedulers.fromExecutorService(asyncExecutor));
    }

    public <T> Flux<T> runOnExecutor(Flux<T> flux) {
        return flux.concatMap(t -> runOnExecutor(Mono.justOrEmpty(t)));
    }
}
