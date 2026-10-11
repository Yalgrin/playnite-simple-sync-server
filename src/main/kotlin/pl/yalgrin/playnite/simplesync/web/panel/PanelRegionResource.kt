package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.util.switchToNotFoundIfEmpty
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.RegionService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelRegionDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/region")
class PanelRegionResource(
    private val service: RegionService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getRegion(@PathVariable id: Long): Mono<PanelRegionDTO> {
        return service.findByIdForPanel(id).switchToNotFoundIfEmpty()
    }

    @PostMapping("/save")
    fun saveRegion(@RequestBody dto: PanelRegionDTO): Mono<PanelRegionDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteRegion(@RequestBody dto: PanelRegionDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterRegions(): Mono<List<PanelRegionDTO>> {
        return service.findAllForPanel()
    }
}
