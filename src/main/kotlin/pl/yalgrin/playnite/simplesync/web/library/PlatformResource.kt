package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.config.PLATFORM
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDTO
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDTO
import pl.yalgrin.playnite.simplesync.service.MetadataService
import pl.yalgrin.playnite.simplesync.service.objects.PlatformService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.NoSuchFileException

@RestController
@RequestMapping("/api")
class PlatformResource(
    private val service: PlatformService,
    private val metadataService: MetadataService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/platform/{id}")
    fun getPlatform(@PathVariable id: Long): Mono<PlatformDTO> {
        return service.findById(id)
    }

    @GetMapping("/platform-diff/{id}")
    fun getPlatformDiff(@PathVariable id: Long): Mono<PlatformDiffDTO> {
        return service.findDiffById(id)
    }

    @GetMapping(
        value = ["/platform-metadata/{id}/{metadataName}"],
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

    @PostMapping("/platform/save")
    fun savePlatform(
        @RequestPart dto: PlatformDTO,
        @RequestPart(required = false) files: Flux<FilePart>?
    ): Mono<PlatformDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, files, true))
    }

    @PostMapping("/platform-diff/save")
    fun savePlatformDiff(
        @RequestPart dto: PlatformDiffDTO,
        @RequestPart(required = false) files: Flux<FilePart>?
    ): Mono<PlatformDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectDiff(dto, files))
    }

    @PostMapping("/platform/delete")
    fun deletePlatform(@RequestBody dto: PlatformDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}