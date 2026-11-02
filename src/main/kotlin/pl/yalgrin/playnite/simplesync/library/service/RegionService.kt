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
import pl.yalgrin.playnite.simplesync.panel.dto.PanelRegionDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelRegionMapper
import reactor.core.publisher.Mono
import java.util.*

@Service
class RegionService(
    private val regionRepository: RegionRepository,
    mapper: RegionMapper,
    panelMapper: PanelRegionMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<RegionDTO, Region, PanelRegionDTO>(
    regionRepository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: RegionDTO): Region {
        return Region(
            playniteId = dto.id
        )
    }

    override fun createEntityFromPanelDTO(dto: PanelRegionDTO): Region {
        return Region(
            playniteId = UUID.randomUUID().toString()
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.REGION
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<Region>): Mono<LibrarySaveResult<Region>> {
        return gameService.updateRegions(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Region>>): Mono<List<ChangeDTO>> {
        return gameService.updateRegionsAfterDelete(results)
    }

    fun findSpecificationIds(): Mono<List<String>> {
        return regionRepository.findSpecificationIds().collectList().transactionalReadOnly()
    }
}