package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.service.AgeRatingService
import pl.yalgrin.playnite.simplesync.panel.dto.PanelAgeRatingDTO
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/panel/age-rating")
class PanelAgeRatingResource(
    private val service: AgeRatingService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getAgeRating(@PathVariable id: Long): Mono<PanelAgeRatingDTO> {
        return service.findByIdForPanel(id)
    }

    @PostMapping("/save")
    fun saveAgeRating(@RequestBody dto: PanelAgeRatingDTO): Mono<PanelAgeRatingDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObjectAndPublishChanges(dto))
    }

    @PostMapping("/delete")
    fun deleteAgeRating(@RequestBody dto: PanelAgeRatingDTO): Mono<Unit> {
        return singleExecutorHelper.runOnExecutor(service.deleteObjectAndPublishChanges(dto))
    }

    @PostMapping("/filter")
    fun filterAgeRatings(): Mono<List<PanelAgeRatingDTO>> {
        return service.findAllForPanel()
    }
}