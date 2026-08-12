package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.AgeRating
import pl.yalgrin.playnite.simplesync.library.dto.AgeRatingDTO
import pl.yalgrin.playnite.simplesync.library.mapper.AgeRatingMapper
import pl.yalgrin.playnite.simplesync.library.repository.AgeRatingRepository

@Service
class AgeRatingService(
    repository: AgeRatingRepository,
    mapper: AgeRatingMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager
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
}