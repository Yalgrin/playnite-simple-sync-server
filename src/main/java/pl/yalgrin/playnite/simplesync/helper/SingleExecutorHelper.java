package pl.yalgrin.playnite.simplesync.helper;

import jakarta.annotation.PreDestroy;
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

    @PreDestroy
    public void shutdown() {
        asyncExecutor.shutdown();
    }

    public <T> Mono<T> runOnExecutor(Mono<T> mono) {
        return Mono.fromCallable(mono::block)
                .subscribeOn(Schedulers.fromExecutorService(asyncExecutor));
    }

    public <T> Flux<T> runOnExecutor(Flux<T> flux) {
        return flux.concatMap(t -> runOnExecutor(Mono.justOrEmpty(t)));
    }
}
