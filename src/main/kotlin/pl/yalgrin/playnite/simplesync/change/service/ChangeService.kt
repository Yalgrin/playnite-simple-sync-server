package pl.yalgrin.playnite.simplesync.change.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.yalgrin.playnite.simplesync.change.mapper.ChangeMessageMapper
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.dto.GameChangeRequestDTO
import pl.yalgrin.playnite.simplesync.repository.ChangeRepository
import pl.yalgrin.playnite.simplesync.security.getSessionClientId
import pl.yalgrin.playnite.simplesync.service.ChangeService
import reactor.core.publisher.Flux

//TODO rewrite the actual service into this
@Service
class ChangeService(
    val changeService: ChangeService,
    val repository: ChangeRepository,
    val changeMessageMapper: ChangeMessageMapper
) {
    @Transactional(readOnly = true)
    fun findFromLastId(lastId: Long?): Flux<ChangeMessage> {
        return getSessionClientId()
            .flatMapMany { repository.findFromLastId(lastId, it) }
            .map { changeMessageMapper.toMessage(it) }
    }

    fun generateChangesForAllObjects(): Flux<ChangeMessage> {
        return changeService.generateChangesForAllObjects().map { changeMessageMapper.toMessage(it) }
    }

    fun generateChangesForGames(dto: GameChangeRequestDTO): Flux<ChangeMessage> {
        return changeService.generateChangesForGames(dto).map { changeMessageMapper.toMessage(it) }
    }
}