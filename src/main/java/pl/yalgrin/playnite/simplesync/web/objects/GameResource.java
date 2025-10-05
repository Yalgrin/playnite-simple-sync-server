package pl.yalgrin.playnite.simplesync.web.objects;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.config.Constants;
import pl.yalgrin.playnite.simplesync.dto.objects.GameDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.GameDiffDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.MetadataService;
import pl.yalgrin.playnite.simplesync.service.objects.GameService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.NoSuchFileException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameResource {
    private final GameService service;
    private final MetadataService metadataService;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/game/{id}")
    public Mono<GameDTO> getGame(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/game-diff/{id}")
    public Mono<GameDiffDTO> getGameDiff(@PathVariable Long id) {
        return service.findDiffById(id);
    }

    @GetMapping(value = "/game-metadata/{id}/{metadataName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<DataBuffer>>> getGameMetadata(@PathVariable String id,
                                                                  @PathVariable String metadataName) {
        return metadataService.getMetadata(Constants.GAME, id, metadataName)
                .flatMap(t -> Mono.justOrEmpty(ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + t.getT1() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).body(t.getT2())))
                .onErrorResume(NoSuchFileException.class, e -> Mono.justOrEmpty(ResponseEntity.notFound().build()))
                .switchIfEmpty(Mono.justOrEmpty(ResponseEntity.notFound().build()));
    }

    @PostMapping("/game/save")
    public Mono<GameDTO> saveGame(@RequestPart GameDTO dto, @RequestPart(required = false) Flux<FilePart> files,
                                  @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId, files, true));
    }

    @PostMapping("/game-diff/save")
    public Mono<GameDTO> saveGameDiff(@RequestPart GameDiffDTO dto, @RequestPart(required = false) Flux<FilePart> files,
                                      @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObjectDiff(dto, clientId, files));
    }

    @PostMapping("/game/delete")
    public Mono<Void> deleteGame(@RequestBody GameDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
