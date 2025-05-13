package pl.yalgrin.playnite.simplesync.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.RegionDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.RegionService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/region")
@RequiredArgsConstructor
public class RegionResource {
    private final RegionService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<RegionDTO> getRegion(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<RegionDTO> saveRegion(@RequestBody RegionDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteRegion(@RequestBody RegionDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
