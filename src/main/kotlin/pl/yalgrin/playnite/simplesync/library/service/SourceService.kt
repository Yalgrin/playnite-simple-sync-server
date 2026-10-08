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
import pl.yalgrin.playnite.simplesync.panel.dto.PanelSourceDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelSourceMapper
import reactor.core.publisher.Mono
import java.util.*

@Service
class SourceService(
    repository: SourceRepository,
    mapper: SourceMapper,
    panelMapper: PanelSourceMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<SourceDTO, Source, PanelSourceDTO>(
    repository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: SourceDTO): Source {
        return Source(
            playniteId = dto.id
        )
    }

    override fun createEntityFromPanelDTO(dto: PanelSourceDTO): Source {
        return Source(
            playniteId = UUID.randomUUID().toString()
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.SOURCE
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<Source>): Mono<LibrarySaveResult<Source>> {
        return gameService.updateSources(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Source>>): Mono<List<ChangeDTO>> {
        return gameService.updateSourcesAfterDelete(results)
    }
}