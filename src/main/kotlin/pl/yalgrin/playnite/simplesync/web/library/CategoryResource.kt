package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.dto.CategoryDTO
import pl.yalgrin.playnite.simplesync.service.objects.CategoryService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/category")
class CategoryResource(
    private val service: CategoryService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getCategory(@PathVariable id: Long): Mono<CategoryDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveCategory(@RequestBody dto: CategoryDTO): Mono<CategoryDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteCategory(@RequestBody dto: CategoryDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}