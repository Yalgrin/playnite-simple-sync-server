package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Source
import pl.yalgrin.playnite.simplesync.library.dto.SourceDTO
import pl.yalgrin.playnite.simplesync.library.mapper.SourceMapper
import pl.yalgrin.playnite.simplesync.library.repository.SourceRepository
import reactor.core.publisher.Mono

@Service
class SourceService(
    repository: SourceRepository,
    mapper: SourceMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<SourceDTO, Source>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: SourceDTO): Source {
        return Source(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.SOURCE
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<SourceDTO>): Mono<LibrarySaveResult<SourceDTO>> {
        return gameService.updateSources(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Source>>): Mono<List<ChangeDTO>> {
        return gameService.updateSourcesAfterDelete(results)
    }
}