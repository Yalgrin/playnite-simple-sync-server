package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.dto.SourceDTO
import pl.yalgrin.playnite.simplesync.service.objects.SourceService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/source")
class SourceResource(
    private val service: SourceService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getSource(@PathVariable id: Long): Mono<SourceDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveSource(@RequestBody dto: SourceDTO): Mono<SourceDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteSource(@RequestBody dto: SourceDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}