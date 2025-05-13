package pl.yalgrin.playnite.simplesync.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.FeatureDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.FeatureService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/feature")
@RequiredArgsConstructor
public class FeatureResource {
    private final FeatureService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<FeatureDTO> getFeature(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<FeatureDTO> saveFeature(@RequestBody FeatureDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteFeature(@RequestBody FeatureDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
