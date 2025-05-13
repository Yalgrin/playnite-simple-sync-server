package pl.yalgrin.playnite.simplesync.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.CompanyDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.CompanyService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyResource {
    private final CompanyService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<CompanyDTO> getCompany(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<CompanyDTO> saveCompany(@RequestBody CompanyDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteCompany(@RequestBody CompanyDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
