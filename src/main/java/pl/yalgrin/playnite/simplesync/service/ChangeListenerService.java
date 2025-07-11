package pl.yalgrin.playnite.simplesync.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
@Slf4j
public class ChangeListenerService {
    private final Sinks.Many<ChangeDTO> sink = Sinks.many().multicast().directBestEffort();

    public Mono<Void> publishChange(ChangeDTO dto) {
        return Mono.fromRunnable(() -> {
            log.debug("publishing {}", dto);
            sink.tryEmitNext(dto);
        });
    }

    public Mono<Void> publishChanges(List<ChangeDTO> dtoList) {
        return Flux.fromIterable(dtoList).concatMap(this::publishChange).then();
    }

    public Flux<ChangeDTO> modificationFlux() {
        return sink.asFlux();
    }
}
