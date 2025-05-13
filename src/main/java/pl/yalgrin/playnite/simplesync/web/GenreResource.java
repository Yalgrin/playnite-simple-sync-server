package pl.yalgrin.playnite.simplesync.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.GenreDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.GenreService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/genre")
@RequiredArgsConstructor
public class GenreResource {
    private final GenreService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<GenreDTO> getGenre(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<GenreDTO> saveGenre(@RequestBody GenreDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteGenre(@RequestBody GenreDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
