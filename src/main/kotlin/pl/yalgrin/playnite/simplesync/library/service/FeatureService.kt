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
import reactor.core.publisher.Mono

@Service
class FeatureService(
    repository: FeatureRepository,
    mapper: FeatureMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<FeatureDTO, Feature>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: FeatureDTO): Feature {
        return Feature(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.FEATURE
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<FeatureDTO>): Mono<LibrarySaveResult<FeatureDTO>> {
        return gameService.updateFeatures(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Feature>>): Mono<List<ChangeDTO>> {
        return gameService.updateFeaturesAfterDelete(results)
    }
}