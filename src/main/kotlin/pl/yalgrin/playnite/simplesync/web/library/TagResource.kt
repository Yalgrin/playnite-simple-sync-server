package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.dto.objects.TagDTO
import pl.yalgrin.playnite.simplesync.helper.SingleExecutorHelper
import pl.yalgrin.playnite.simplesync.service.objects.TagService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/tag")
class TagResource(
    private val service: TagService,
    private val singleExecutorHelper: SingleExecutorHelper
) {

    @GetMapping("/{id}")
    fun getTag(@PathVariable id: Long): Mono<TagDTO> {
        return service.findById(id)
    }

    @PostMapping("/save")
    fun saveTag(@RequestBody dto: TagDTO): Mono<TagDTO> {
        return singleExecutorHelper.runOnExecutor(service.saveObject(dto))
    }

    @PostMapping("/delete")
    fun deleteTag(@RequestBody dto: TagDTO): Mono<Void> {
        return singleExecutorHelper.runOnExecutor(service.deleteObject(dto))
    }
}