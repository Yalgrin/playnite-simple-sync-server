package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.AgeRating
import pl.yalgrin.playnite.simplesync.panel.dto.PanelAgeRatingDTO

@Component
class PanelAgeRatingMapper : PanelObjectMapperImpl<AgeRating, PanelAgeRatingDTO>() {
    override fun createDTO(): PanelAgeRatingDTO = PanelAgeRatingDTO()
}