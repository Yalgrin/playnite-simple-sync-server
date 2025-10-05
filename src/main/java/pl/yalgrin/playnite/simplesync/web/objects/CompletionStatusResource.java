package pl.yalgrin.playnite.simplesync.web.objects;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.objects.CompletionStatusDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.objects.CompletionStatusService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/completion-status")
@RequiredArgsConstructor
public class CompletionStatusResource {
    private final CompletionStatusService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<CompletionStatusDTO> getCompletionStatus(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<CompletionStatusDTO> saveCompletionStatus(@RequestBody CompletionStatusDTO dto,
                                                          @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteCompletionStatus(@RequestBody CompletionStatusDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
