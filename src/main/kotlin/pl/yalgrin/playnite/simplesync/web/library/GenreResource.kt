package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.dto.objects.GenreDTO
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.service.objects.GenreService
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
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteGenre(@RequestBody dto: GenreDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}