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
import reactor.core.publisher.Mono

@Service
class SeriesService(
    repository: SeriesRepository,
    mapper: SeriesMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<SeriesDTO, Series>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: SeriesDTO): Series {
        return Series(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.SERIES
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<SeriesDTO>): Mono<LibrarySaveResult<SeriesDTO>> {
        return gameService.updateSeries(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Series>>): Mono<List<ChangeDTO>> {
        return gameService.updateSeriesAfterDelete(results)
    }
}