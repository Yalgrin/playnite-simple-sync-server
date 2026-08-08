package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.dto.objects.AgeRatingDTO
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.service.objects.AgeRatingService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/age-rating")
class AgeRatingResource(
    private val service: AgeRatingService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getAgeRating(@PathVariable id: Long): Mono<AgeRatingDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveAgeRating(@RequestBody dto: AgeRatingDTO): Mono<AgeRatingDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteAgeRating(@RequestBody dto: AgeRatingDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}