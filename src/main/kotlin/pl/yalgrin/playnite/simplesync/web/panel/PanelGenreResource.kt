package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.util.switchToNotFoundIfEmpty
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.GenreService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGenreDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/genre")
class PanelGenreResource(
    private val service: GenreService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getGenre(@PathVariable id: Long): Mono<PanelGenreDTO> {
        return service.findByIdForPanel(id).switchToNotFoundIfEmpty()
    }

    @PostMapping("/save")
    fun saveGenre(@RequestBody dto: PanelGenreDTO): Mono<PanelGenreDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteGenre(@RequestBody dto: PanelGenreDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterGenres(): Mono<List<PanelGenreDTO>> {
        return service.findAllForPanel()
    }
}
