package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Region
import pl.yalgrin.playnite.simplesync.library.dto.RegionDTO
import pl.yalgrin.playnite.simplesync.library.mapper.RegionMapper
import pl.yalgrin.playnite.simplesync.library.repository.RegionRepository
import reactor.core.publisher.Mono

@Service
class RegionService(
    repository: RegionRepository,
    mapper: RegionMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<RegionDTO, Region>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: RegionDTO): Region {
        return Region(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.REGION
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<RegionDTO>): Mono<LibrarySaveResult<RegionDTO>> {
        return gameService.updateRegions(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Region>>): Mono<List<ChangeDTO>> {
        return gameService.updateRegionsAfterDelete(results)
    }
}