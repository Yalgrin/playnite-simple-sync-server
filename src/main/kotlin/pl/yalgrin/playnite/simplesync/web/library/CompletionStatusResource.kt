package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.library.dto.CompletionStatusDTO
import pl.yalgrin.playnite.simplesync.service.objects.CompletionStatusService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/completion-status")
class CompletionStatusResource(
    private val service: CompletionStatusService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getCompletionStatus(@PathVariable id: Long): Mono<CompletionStatusDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveCompletionStatus(@RequestBody dto: CompletionStatusDTO): Mono<CompletionStatusDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteCompletionStatus(@RequestBody dto: CompletionStatusDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}