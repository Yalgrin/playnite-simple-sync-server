package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.dto.GenreDTO
import pl.yalgrin.playnite.simplesync.library.service.GenreService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/genre")
class GenreResource(
    private val service: GenreService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getGenre(@PathVariable id: Long): Mono<GenreDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveGenre(@RequestBody dto: GenreDTO): Mono<GenreDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteGenre(@RequestBody dto: GenreDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }
}