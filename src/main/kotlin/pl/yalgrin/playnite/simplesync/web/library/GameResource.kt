package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.config.GAME
import pl.yalgrin.playnite.simplesync.dto.objects.GameDTO
import pl.yalgrin.playnite.simplesync.dto.objects.GameDiffDTO
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.service.MetadataService
import pl.yalgrin.playnite.simplesync.service.objects.GameService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.NoSuchFileException

@RestController
@RequestMapping("/api")
class GameResource(
    private val service: GameService,
    private val metadataService: MetadataService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/game/{id}")
    fun getGame(@PathVariable id: Long): Mono<GameDTO> {
        return service.findById(id)
    }

    @GetMapping("/game-diff/{id}")
    fun getGameDiff(@PathVariable id: Long): Mono<GameDiffDTO> {
        return service.findDiffById(id)
    }

    @GetMapping(value = ["/game-metadata/{id}/{metadataName}"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getGameMetadata(
        @PathVariable id: String,
        @PathVariable metadataName: String
    ): Mono<ResponseEntity<Flux<DataBuffer>>> {
        return metadataService.getMetadata(GAME, id, metadataName)
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

    @PostMapping("/game/save")
    fun saveGame(@RequestPart dto: GameDTO, @RequestPart(required = false) files: Flux<FilePart>?): Mono<GameDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, files, true))
    }

    @PostMapping("/game-diff/save")
    fun saveGameDiff(
        @RequestPart dto: GameDiffDTO,
        @RequestPart(required = false) files: Flux<FilePart>?
    ): Mono<GameDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectDiff(dto, files))
    }

    @PostMapping("/game/delete")
    fun deleteGame(@RequestBody dto: GameDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}