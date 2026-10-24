package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.config.PLATFORM
import pl.yalgrin.playnite.simplesync.common.util.switchToNotFoundIfEmpty
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.MetadataService
import pl.yalgrin.playnite.simplesync.library.service.PlatformService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelPlatformDTO
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.NoSuchFileException

@RestController
@RequestMapping("/api/panel/platform")
class PanelPlatformResource(
    private val service: PlatformService,
    private val metadataService: MetadataService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getPlatform(@PathVariable id: Long): Mono<PanelPlatformDTO> {
        return service.findByIdForPanel(id).switchToNotFoundIfEmpty()
    }

    @GetMapping(
        value = ["/{id}/metadata/{metadataName}/{hash}"],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun getPlatformMetadata(
        @PathVariable id: String,
        @PathVariable metadataName: String
    ): Mono<ResponseEntity<Flux<DataBuffer>>> {
        return metadataService.getMetadata(PLATFORM, id, metadataName)
            .flatMap { t ->
                Mono.justOrEmpty(
                    ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${t.t1}\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(t.t2)
                )
            }
            .onErrorResume(NoSuchFileException::class.java) {
                Mono.justOrEmpty(ResponseEntity.notFound().build())
            }
            .switchIfEmpty(Mono.justOrEmpty(ResponseEntity.notFound().build()))
    }

    @PostMapping("/save")
    fun savePlatform(
        @RequestPart dto: PanelPlatformDTO,
        @RequestPart(required = false) files: Flux<FilePart>
    ): Mono<PanelPlatformDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto, files, true))
    }

    @PostMapping("/delete")
    fun deletePlatform(@RequestBody dto: PanelPlatformDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterPlatforms(): Mono<List<PanelPlatformDTO>> {
        return service.findAllForPanel()
    }
}