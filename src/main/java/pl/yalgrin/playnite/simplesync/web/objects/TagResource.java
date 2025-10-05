package pl.yalgrin.playnite.simplesync.web.objects;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.objects.TagDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.objects.TagService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagResource {
    private final TagService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<TagDTO> getTag(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<TagDTO> saveTag(@RequestBody TagDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteTag(@RequestBody TagDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
