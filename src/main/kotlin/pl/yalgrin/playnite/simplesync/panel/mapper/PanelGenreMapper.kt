package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Genre
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGenreDTO

@Component
class PanelGenreMapper : PanelObjectMapperImpl<Genre, PanelGenreDTO>() {
    override fun createDTO(): PanelGenreDTO = PanelGenreDTO()
}
