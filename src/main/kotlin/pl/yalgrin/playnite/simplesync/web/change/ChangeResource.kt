package pl.yalgrin.playnite.simplesync.web.change

import org.springframework.web.bind.annotation.*
import pl.yalgrin.playnite.simplesync.change.dto.GameChangeRequestDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/change")
class ChangeResource(
    val service: ChangeService
) {

    @GetMapping
    fun fetchChanges(@RequestParam(required = false) lastChangeId: Long?): Flux<ChangeMessage> {
        return service.findFromLastId(lastChangeId)
    }

    @GetMapping("/all")
    fun fetchAllObjects(): Flux<ChangeMessage> {
        return service.generateChangesForAllObjects()
    }

    @PostMapping("/games")
    fun fetchSelectedGameChanges(@RequestBody dto: GameChangeRequestDTO): Flux<ChangeMessage> {
        return service.generateChangesForGames(dto)
    }
}