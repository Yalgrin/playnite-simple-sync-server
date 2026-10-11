package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.config.PLUGIN
import pl.yalgrin.playnite.simplesync.common.util.switchToNotFoundIfEmpty
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.LibraryPluginService
import pl.yalgrin.playnite.simplesync.library.service.MetadataService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelLibraryPluginDTO
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.NoSuchFileException

@RestController
@RequestMapping("/api/panel/library-plugin")
class PanelLibraryPluginResource(
    private val service: LibraryPluginService,
    private val metadataService: MetadataService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getLibraryPlugin(@PathVariable id: Long): Mono<PanelLibraryPluginDTO> {
        return service.findByIdForPanel(id).switchToNotFoundIfEmpty()
    }

    @GetMapping(
        value = ["/{id}/metadata/{metadataName}"],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun getLibraryPluginMetadata(
        @PathVariable id: String,
        @PathVariable metadataName: String
    ): Mono<ResponseEntity<Flux<DataBuffer>>> {
        return metadataService.getMetadata(PLUGIN, id, metadataName)
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
    fun saveLibraryPlugin(
        @RequestPart dto: PanelLibraryPluginDTO,
        @RequestPart(required = false) files: Flux<FilePart>
    ): Mono<PanelLibraryPluginDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto, files, true))
    }

    @PostMapping("/delete")
    fun deleteLibraryPlugin(@RequestBody dto: PanelLibraryPluginDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterLibraryPlugins(): Mono<List<PanelLibraryPluginDTO>> {
        return service.findAllForPanel()
    }
}