package pl.yalgrin.playnite.simplesync.web;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.config.Constants;
import pl.yalgrin.playnite.simplesync.dto.PlatformDTO;
import pl.yalgrin.playnite.simplesync.dto.PlatformDiffDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.MetadataService;
import pl.yalgrin.playnite.simplesync.service.PlatformService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.NoSuchFileException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlatformResource {
    private final PlatformService service;
    private final MetadataService metadataService;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/platform/{id}")
    public Mono<PlatformDTO> getPlatform(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/platform-diff/{id}")
    public Mono<PlatformDiffDTO> getPlatformDiff(@PathVariable Long id) {
        return service.findDiffById(id);
    }

    @GetMapping(value = "/platform-metadata/{id}/{metadataName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<DataBuffer>>> getPlatformMetadata(@PathVariable String id,
                                                                      @PathVariable String metadataName) {
        return metadataService.getMetadata(Constants.PLATFORM, id, metadataName)
                .flatMap(t -> Mono.justOrEmpty(ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + t.getT1() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).body(t.getT2())))
                .onErrorResume(NoSuchFileException.class, e -> Mono.justOrEmpty(ResponseEntity.notFound().build()))
                .switchIfEmpty(Mono.justOrEmpty(ResponseEntity.notFound().build()));
    }

    @PostMapping("/platform/save")
    public Mono<PlatformDTO> savePlatform(@RequestPart PlatformDTO dto,
                                          @RequestPart(required = false) Flux<FilePart> files,
                                          @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId, files, true));
    }

    @PostMapping("/platform-diff/save")
    public Mono<PlatformDTO> savePlatformDiff(@RequestPart PlatformDiffDTO dto,
                                              @RequestPart(required = false) Flux<FilePart> files,
                                              @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObjectDiff(dto, clientId, files));
    }

    @PostMapping("/platform/delete")
    public Mono<Void> deletePlatform(@RequestBody PlatformDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
