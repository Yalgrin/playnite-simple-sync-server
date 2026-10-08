package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Feature
import pl.yalgrin.playnite.simplesync.library.dto.FeatureDTO
import pl.yalgrin.playnite.simplesync.library.mapper.FeatureMapper
import pl.yalgrin.playnite.simplesync.library.repository.FeatureRepository
import pl.yalgrin.playnite.simplesync.panel.dto.PanelFeatureDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelFeatureMapper
import reactor.core.publisher.Mono
import java.util.*

@Service
class FeatureService(
    repository: FeatureRepository,
    mapper: FeatureMapper,
    panelMapper: PanelFeatureMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<FeatureDTO, Feature, PanelFeatureDTO>(
    repository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: FeatureDTO): Feature {
        return Feature(
            playniteId = dto.id
        )
    }

    override fun createEntityFromPanelDTO(dto: PanelFeatureDTO): Feature {
        return Feature(
            playniteId = UUID.randomUUID().toString()
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.FEATURE
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<Feature>): Mono<LibrarySaveResult<Feature>> {
        return gameService.updateFeatures(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Feature>>): Mono<List<ChangeDTO>> {
        return gameService.updateFeaturesAfterDelete(results)
    }
}