package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Tag
import pl.yalgrin.playnite.simplesync.panel.dto.PanelTagDTO

@Component
class PanelTagMapper : PanelObjectMapperImpl<Tag, PanelTagDTO>() {
    override fun createDTO(): PanelTagDTO = PanelTagDTO()
}
