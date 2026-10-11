package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.config.GAME
import pl.yalgrin.playnite.simplesync.common.util.switchToNotFoundIfEmpty
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.GameService
import pl.yalgrin.playnite.simplesync.library.service.MetadataService
import pl.yalgrin.playnite.simplesync.panel.dto.GameListFilterDTO
import pl.yalgrin.playnite.simplesync.panel.dto.GameListResultDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGameDTO
import pl.yalgrin.playnite.simplesync.panel.service.GameListService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.NoSuchFileException

@RestController
@RequestMapping("/api/panel/game")
class PanelGameResource(
    private val service: GameService,
    private val metadataService: MetadataService,
    private val gameListService: GameListService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getGame(@PathVariable id: Long): Mono<PanelGameDTO> {
        return service.findByIdForPanel(id).switchToNotFoundIfEmpty()
    }

    @GetMapping(value = ["/{id}/metadata/{metadataName}/{hash}"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
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

    @PostMapping("/save")
    fun saveGame(
        @RequestPart dto: PanelGameDTO,
        @RequestPart(required = false) files: Flux<FilePart>
    ): Mono<PanelGameDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto, files, true))
    }

    @PostMapping("/delete")
    fun deleteGame(@RequestBody dto: PanelGameDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun getGameList(@RequestBody filter: GameListFilterDTO): Mono<GameListResultDTO> {
        return gameListService.findByFilter(filter)
    }
}