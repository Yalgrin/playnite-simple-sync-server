package pl.yalgrin.playnite.simplesync.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.yalgrin.playnite.simplesync.dto.CategoryDTO;
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper;
import pl.yalgrin.playnite.simplesync.service.CategoryService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryResource {
    private final CategoryService service;
    private final SingleExecutorHelper singleExecutorHelper;

    @GetMapping("/{id}")
    public Mono<CategoryDTO> getCategory(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/save")
    public Mono<CategoryDTO> saveCategory(@RequestBody CategoryDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto, clientId));
    }

    @PostMapping("/delete")
    public Mono<Void> deleteCategory(@RequestBody CategoryDTO dto, @RequestParam String clientId) {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto, clientId));
    }
}
