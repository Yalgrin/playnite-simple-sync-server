package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Series
import pl.yalgrin.playnite.simplesync.panel.dto.PanelSeriesDTO

@Component
class PanelSeriesMapper : PanelObjectMapperImpl<Series, PanelSeriesDTO>() {
    override fun createDTO(): PanelSeriesDTO = PanelSeriesDTO()
}
