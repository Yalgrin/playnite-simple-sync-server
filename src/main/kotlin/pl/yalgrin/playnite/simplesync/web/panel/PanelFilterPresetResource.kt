package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.util.switchToNotFoundIfEmpty
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.FilterPresetService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelFilterPresetDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/filter-preset")
class PanelFilterPresetResource(
    private val service: FilterPresetService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getFeature(@PathVariable id: Long): Mono<PanelFilterPresetDTO> {
        return service.findByIdForPanel(id).switchToNotFoundIfEmpty()
    }

    @PostMapping("/save")
    fun saveFeature(@RequestBody dto: PanelFilterPresetDTO): Mono<PanelFilterPresetDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteFeature(@RequestBody dto: PanelFilterPresetDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterFilterPresets(): Mono<List<PanelFilterPresetDTO>> {
        return service.findAllForPanel()
    }
}
