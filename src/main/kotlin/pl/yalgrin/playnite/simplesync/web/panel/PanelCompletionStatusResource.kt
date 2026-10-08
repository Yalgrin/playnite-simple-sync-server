package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.CompletionStatusService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCompletionStatusDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/completion-status")
class PanelCompletionStatusResource(
    private val service: CompletionStatusService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getCompletionStatus(@PathVariable id: Long): Mono<PanelCompletionStatusDTO> {
        return service.findByIdForPanel(id)
    }

    @PostMapping("/save")
    fun saveCompletionStatus(@RequestBody dto: PanelCompletionStatusDTO): Mono<PanelCompletionStatusDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteCompletionStatus(@RequestBody dto: PanelCompletionStatusDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterCompletionStatuses(): Mono<List<PanelCompletionStatusDTO>> {
        return service.findAllForPanel()
    }
}
