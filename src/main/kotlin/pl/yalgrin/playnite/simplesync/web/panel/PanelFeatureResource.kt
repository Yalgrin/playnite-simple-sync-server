package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.FeatureService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelFeatureDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/feature")
class PanelFeatureResource(
    private val service: FeatureService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getFeature(@PathVariable id: Long): Mono<PanelFeatureDTO> {
        return service.findByIdForPanel(id)
    }

    @PostMapping("/save")
    fun saveFeature(@RequestBody dto: PanelFeatureDTO): Mono<PanelFeatureDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteFeature(@RequestBody dto: PanelFeatureDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterFeatures(): Mono<List<PanelFeatureDTO>> {
        return service.findAllForPanel()
    }
}
