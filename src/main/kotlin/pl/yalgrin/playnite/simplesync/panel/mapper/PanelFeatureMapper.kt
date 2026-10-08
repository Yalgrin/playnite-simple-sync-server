package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Feature
import pl.yalgrin.playnite.simplesync.panel.dto.PanelFeatureDTO

@Component
class PanelFeatureMapper : PanelObjectMapperImpl<Feature, PanelFeatureDTO>() {
    override fun createDTO(): PanelFeatureDTO = PanelFeatureDTO()
}
