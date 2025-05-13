package pl.yalgrin.playnite.simplesync.web;


import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import pl.yalgrin.playnite.simplesync.dto.GameChangeRequestDTO;
import pl.yalgrin.playnite.simplesync.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.service.ChangeService;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/change")
@RequiredArgsConstructor
public class ChangeResource {

    private final ChangeService service;
    private final ChangeListenerService listenerService;

    @GetMapping
    public Flux<ChangeDTO> fetchChanges(@RequestParam(required = false) Long lastChangeId) {
        return service.findFromLastId(lastChangeId);
    }

    @GetMapping(value = "/all")
    public Flux<ChangeDTO> fetchAllObjects() {
        return service.generateChangesForAllObjects();
    }

    @PostMapping(value = "/games")
    public Flux<ChangeDTO> fetchSelectedGameChanges(@RequestBody GameChangeRequestDTO dto) {
        return service.generateChangesForGames(dto);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChangeDTO>> getChangeStream(@RequestParam(required = false) Long lastChangeId) {
        return Flux.merge(
                listenerService.modificationFlux().map(dto -> ServerSentEvent.<ChangeDTO>builder().data(dto).build()),
                Flux.interval(Duration.ofSeconds(30)).map(i -> ChangeResource.createHeartbeat()),
                Flux.just(ChangeResource.createHeartbeat()),
                lastChangeId != null ? service.findFromLastId(lastChangeId)
                        .map(dto -> ServerSentEvent.<ChangeDTO>builder().data(dto).build()) : Flux.empty());
    }

    private static <T> ServerSentEvent<T> createHeartbeat() {
        return ServerSentEvent.<T>builder().comment("heartbeat").build();
    }
}
