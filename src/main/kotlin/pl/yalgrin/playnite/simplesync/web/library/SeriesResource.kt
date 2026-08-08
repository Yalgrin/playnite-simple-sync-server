package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.dto.objects.SeriesDTO
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.service.objects.SeriesService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/series")
class SeriesResource(
    private val service: SeriesService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getSeries(@PathVariable id: Long): Mono<SeriesDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveSeries(@RequestBody dto: SeriesDTO): Mono<SeriesDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteSeries(@RequestBody dto: SeriesDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}