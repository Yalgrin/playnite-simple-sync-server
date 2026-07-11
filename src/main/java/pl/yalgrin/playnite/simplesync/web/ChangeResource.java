package pl.yalgrin.playnite.simplesync.web;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import pl.yalgrin.playnite.simplesync.dto.GameChangeRequestDTO;
import pl.yalgrin.playnite.simplesync.service.ChangeService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/change")
@RequiredArgsConstructor
public class ChangeResource {

    private final ChangeService service;

    @GetMapping
    public Flux<ChangeDTO> fetchChanges(@RequestParam(required = false) Long lastChangeId) {
        return service.findFromLastId(lastChangeId);
    }

    @GetMapping(value = "/all")
    public Flux<ChangeDTO> fetchAllObjects() {
        return service.generateChangesForAllObjects();
    }

    @PostMapping(value = "/games")
    public Flux<ChangeDTO> fetchSelectedGameChanges(@RequestBody GameChangeRequestDTO dto) {
        return service.generateChangesForGames(dto);
    }
}
