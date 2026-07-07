package pl.yalgrin.playnite.simplesync.web.objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.objects.AgeRatingDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.objects.AgeRatingService;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/api/age-rating")
@RequiredArgsConstructor
public class AgeRatingResource {
    private final AgeRatingService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<AgeRatingDTO> getAgeRating(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<AgeRatingDTO> saveAgeRating(@RequestBody AgeRatingDTO dto) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteAgeRating(@RequestBody AgeRatingDTO dto) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto));
    }
}
