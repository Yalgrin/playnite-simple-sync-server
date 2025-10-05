package pl.yalgrin.playnite.simplesync.web.objects;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.objects.SourceDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.objects.SourceService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/source")
@RequiredArgsConstructor
public class SourceResource {
    private final SourceService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<SourceDTO> getSource(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<SourceDTO> saveSource(@RequestBody SourceDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteSource(@RequestBody SourceDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
