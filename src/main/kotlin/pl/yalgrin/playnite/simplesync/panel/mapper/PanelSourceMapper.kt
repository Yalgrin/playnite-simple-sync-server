package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Source
import pl.yalgrin.playnite.simplesync.panel.dto.PanelSourceDTO

@Component
class PanelSourceMapper : PanelObjectMapperImpl<Source, PanelSourceDTO>() {
    override fun createDTO(): PanelSourceDTO = PanelSourceDTO()
}
