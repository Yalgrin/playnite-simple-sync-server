package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.CategoryService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCategoryDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/category")
class PanelCategoryResource(
    private val service: CategoryService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getCategory(@PathVariable id: Long): Mono<PanelCategoryDTO> {
        return service.findByIdForPanel(id)
    }

    @PostMapping("/save")
    fun saveCategory(@RequestBody dto: PanelCategoryDTO): Mono<PanelCategoryDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteCategory(@RequestBody dto: PanelCategoryDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterCategories(): Mono<List<PanelCategoryDTO>> {
        return service.findAllForPanel()
    }
}
