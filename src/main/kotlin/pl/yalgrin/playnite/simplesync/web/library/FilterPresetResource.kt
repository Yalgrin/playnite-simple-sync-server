package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.dto.FilterPresetDTO
import pl.yalgrin.playnite.simplesync.service.objects.FilterPresetService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/filter-preset")
class FilterPresetResource(
    private val service: FilterPresetService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getFilterPreset(@PathVariable id: Long): Mono<FilterPresetDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveFilterPreset(@RequestBody dto: FilterPresetDTO): Mono<FilterPresetDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteFilterPreset(@RequestBody dto: FilterPresetDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}