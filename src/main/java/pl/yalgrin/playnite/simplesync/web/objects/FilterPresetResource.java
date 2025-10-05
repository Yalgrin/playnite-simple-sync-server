package pl.yalgrin.playnite.simplesync.web.objects;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.objects.FilterPresetDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.objects.FilterPresetService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/filter-preset")
@RequiredArgsConstructor
public class FilterPresetResource {
    private final FilterPresetService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<FilterPresetDTO> getFilterPreset(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<FilterPresetDTO> saveFilterPreset(@RequestBody FilterPresetDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteFilterPreset(@RequestBody FilterPresetDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
