package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.CompletionStatus
import pl.yalgrin.playnite.simplesync.library.dto.CompletionStatusDTO
import pl.yalgrin.playnite.simplesync.library.mapper.CompletionStatusMapper
import pl.yalgrin.playnite.simplesync.library.repository.CompletionStatusRepository
import reactor.core.publisher.Mono

@Service
class CompletionStatusService(
    repository: CompletionStatusRepository,
    mapper: CompletionStatusMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<CompletionStatusDTO, CompletionStatus>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: CompletionStatusDTO): CompletionStatus {
        return CompletionStatus(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.COMPLETION_STATUS
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<CompletionStatusDTO>): Mono<LibrarySaveResult<CompletionStatusDTO>> {
        return gameService.updateCompletionStatus(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<CompletionStatus>>): Mono<List<ChangeDTO>> {
        return gameService.updateCompletionStatusesAfterDelete(results)
    }
}