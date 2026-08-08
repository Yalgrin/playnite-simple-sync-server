package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.dto.objects.FeatureDTO
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.service.objects.FeatureService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/feature")
class FeatureResource(
    private val service: FeatureService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getFeature(@PathVariable id: Long): Mono<FeatureDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveFeature(@RequestBody dto: FeatureDTO): Mono<FeatureDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteFeature(@RequestBody dto: FeatureDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}