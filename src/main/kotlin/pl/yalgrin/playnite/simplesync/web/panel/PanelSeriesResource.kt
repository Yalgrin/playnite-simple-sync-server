package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.SeriesService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelSeriesDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/series")
class PanelSeriesResource(
    private val service: SeriesService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getSeries(@PathVariable id: Long): Mono<PanelSeriesDTO> {
        return service.findByIdForPanel(id)
    }

    @PostMapping("/save")
    fun saveSeries(@RequestBody dto: PanelSeriesDTO): Mono<PanelSeriesDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteSeries(@RequestBody dto: PanelSeriesDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterSeries(): Mono<List<PanelSeriesDTO>> {
        return service.findAllForPanel()
    }
}
