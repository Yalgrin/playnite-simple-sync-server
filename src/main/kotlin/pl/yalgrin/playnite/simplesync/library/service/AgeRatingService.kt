package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.AgeRating
import pl.yalgrin.playnite.simplesync.library.dto.AgeRatingDTO
import pl.yalgrin.playnite.simplesync.library.mapper.AgeRatingMapper
import pl.yalgrin.playnite.simplesync.library.repository.AgeRatingRepository
import reactor.core.publisher.Mono

@Service
class AgeRatingService(
    repository: AgeRatingRepository,
    mapper: AgeRatingMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<AgeRatingDTO, AgeRating>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: AgeRatingDTO): AgeRating {
        return AgeRating(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.AGE_RATING
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<AgeRatingDTO>): Mono<LibrarySaveResult<AgeRatingDTO>> {
        return gameService.updateAgeRatings(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<AgeRating>>): Mono<List<ChangeDTO>> {
        return gameService.updateAgeRatingsAfterDelete(results)
    }
}