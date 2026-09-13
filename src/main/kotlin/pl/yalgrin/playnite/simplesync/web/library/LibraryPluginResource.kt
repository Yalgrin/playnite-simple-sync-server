package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.config.PLUGIN
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDiffDTO
import pl.yalgrin.playnite.simplesync.library.service.LibraryPluginService
import pl.yalgrin.playnite.simplesync.library.service.MetadataService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.NoSuchFileException

@RestController
@RequestMapping("/api")
class LibraryPluginResource(
    private val service: LibraryPluginService,
    private val metadataService: MetadataService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/library-plugin/{id}")
    fun getLibraryPlugin(@PathVariable id: Long): Mono<LibraryPluginDTO> {
        return service.findById(id)
    }

    @GetMapping("/library-plugin-diff/{id}")
    fun getLibraryPluginDiff(@PathVariable id: Long): Mono<LibraryPluginDiffDTO> {
        return service.findDiffById(id)
    }

    @GetMapping(
        value = ["/library-plugin-metadata/{id}/{metadataName}"],
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

    @PostMapping("/library-plugin/save")
    fun saveLibraryPlugin(
        @RequestPart dto: LibraryPluginDTO,
        @RequestPart(required = false) files: Flux<FilePart>
    ): Mono<LibraryPluginDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto, files, true))
    }

    @PostMapping("/library-plugin-diff/save")
    fun saveLibraryPluginDiff(
        @RequestPart dto: LibraryPluginDiffDTO,
        @RequestPart(required = false) files: Flux<FilePart>
    ): Mono<LibraryPluginDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectDiffAndPublishChanges(dto, files))
    }

    @PostMapping("/library-plugin/delete")
    fun deleteLibraryPlugin(@RequestBody dto: LibraryPluginDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }
}