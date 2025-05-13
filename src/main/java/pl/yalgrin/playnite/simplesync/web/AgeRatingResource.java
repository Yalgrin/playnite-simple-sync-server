package pl.yalgrin.playnite.simplesync.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.AgeRatingDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.AgeRatingService;
import reactor.core.publisher.Mono;

@RestController
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
    public Mono<AgeRatingDTO> saveAgeRating(@RequestBody AgeRatingDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteAgeRating(@RequestBody AgeRatingDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
