package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.config.GAME
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.library.service.MetadataService
import pl.yalgrin.playnite.simplesync.panel.dto.GameDetailsDTO
import pl.yalgrin.playnite.simplesync.panel.dto.GameListFilterDTO
import pl.yalgrin.playnite.simplesync.panel.dto.GameListResultDTO
import pl.yalgrin.playnite.simplesync.panel.service.GameListService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.NoSuchFileException

@RestController
@RequestMapping("/api/panel/game")
class PanelGameListResource(
    private val gameRepository: GameRepository,
    private val metadataService: MetadataService,
    private val gameListService: GameListService
) {

    @GetMapping("/{id}")
    fun getGameDetails(@PathVariable("id") id: Long): Mono<GameDetailsDTO> {
        return gameRepository.findById(id).flatMap { game ->
            game.extractDbModelOrEmpty().map { GameDetailsDTO(game.id!!, game.name!!, it.description) }
        }
    }

    @GetMapping("/{id}/metadata/{name}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getGameDetails(
        @PathVariable("id") id: String,
        @PathVariable name: String
    ): Mono<ResponseEntity<Flux<DataBuffer>>> {
        return metadataService.getMetadata(GAME, id, name)
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

    @PostMapping("/filter")
    fun getGameList(@RequestBody filter: GameListFilterDTO): Mono<GameListResultDTO> {
        return gameListService.findByFilter(filter)
    }
}
