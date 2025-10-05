package pl.yalgrin.playnite.simplesync.web.objects;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.objects.SeriesDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.objects.SeriesService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesResource {
    private final SeriesService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<SeriesDTO> getSeries(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<SeriesDTO> saveSeries(@RequestBody SeriesDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteSeries(@RequestBody SeriesDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
