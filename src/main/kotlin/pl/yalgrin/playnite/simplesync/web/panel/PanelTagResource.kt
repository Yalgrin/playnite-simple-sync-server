package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.common.util.switchToNotFoundIfEmpty
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.TagService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelTagDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/tag")
class PanelTagResource(
    private val service: TagService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getTag(@PathVariable id: Long): Mono<PanelTagDTO> {
        return service.findByIdForPanel(id).switchToNotFoundIfEmpty()
    }

    @PostMapping("/save")
    fun saveTag(@RequestBody dto: PanelTagDTO): Mono<PanelTagDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteTag(@RequestBody dto: PanelTagDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterTags(): Mono<List<PanelTagDTO>> {
        return service.findAllForPanel()
    }
}
