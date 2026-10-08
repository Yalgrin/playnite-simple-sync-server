package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Series
import pl.yalgrin.playnite.simplesync.library.dto.SeriesDTO
import pl.yalgrin.playnite.simplesync.library.mapper.SeriesMapper
import pl.yalgrin.playnite.simplesync.library.repository.SeriesRepository
import pl.yalgrin.playnite.simplesync.panel.dto.PanelSeriesDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelSeriesMapper
import reactor.core.publisher.Mono
import java.util.*

@Service
class SeriesService(
    repository: SeriesRepository,
    mapper: SeriesMapper,
    panelMapper: PanelSeriesMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<SeriesDTO, Series, PanelSeriesDTO>(
    repository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: SeriesDTO): Series {
        return Series(
            playniteId = dto.id
        )
    }

    override fun createEntityFromPanelDTO(dto: PanelSeriesDTO): Series {
        return Series(
            playniteId = UUID.randomUUID().toString()
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.SERIES
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<Series>): Mono<LibrarySaveResult<Series>> {
        return gameService.updateSeries(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Series>>): Mono<List<ChangeDTO>> {
        return gameService.updateSeriesAfterDelete(results)
    }
}