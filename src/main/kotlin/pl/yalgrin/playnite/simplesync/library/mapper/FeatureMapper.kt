package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Feature
import pl.yalgrin.playnite.simplesync.library.dto.FeatureDTO

@Component
class FeatureMapper : LibraryObjectMapperImpl<Feature, FeatureDTO>() {
    override fun createDTO(): FeatureDTO = FeatureDTO()
}