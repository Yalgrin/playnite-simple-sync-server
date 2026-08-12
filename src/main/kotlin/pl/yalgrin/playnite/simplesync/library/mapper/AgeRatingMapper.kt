package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.AgeRating
import pl.yalgrin.playnite.simplesync.library.dto.AgeRatingDTO

@Component
class AgeRatingMapper : LibraryObjectMapperImpl<AgeRating, AgeRatingDTO>() {
    override fun createDTO(): AgeRatingDTO = AgeRatingDTO()
}