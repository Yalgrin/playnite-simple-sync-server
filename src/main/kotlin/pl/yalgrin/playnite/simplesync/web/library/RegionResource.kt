package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.dto.RegionDTO
import pl.yalgrin.playnite.simplesync.library.service.RegionService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/region")
class RegionResource(
    private val service: RegionService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getRegion(@PathVariable id: Long): Mono<RegionDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveRegion(@RequestBody dto: RegionDTO): Mono<RegionDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteRegion(@RequestBody dto: RegionDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }
}