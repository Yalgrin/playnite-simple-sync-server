package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.SourceService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelSourceDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/source")
class PanelSourceResource(
    private val service: SourceService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getSource(@PathVariable id: Long): Mono<PanelSourceDTO> {
        return service.findByIdForPanel(id)
    }

    @PostMapping("/save")
    fun saveSource(@RequestBody dto: PanelSourceDTO): Mono<PanelSourceDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteSource(@RequestBody dto: PanelSourceDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterSources(): Mono<List<PanelSourceDTO>> {
        return service.findAllForPanel()
    }
}
