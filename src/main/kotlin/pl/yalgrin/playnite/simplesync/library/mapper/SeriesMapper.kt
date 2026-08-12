package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Series
import pl.yalgrin.playnite.simplesync.library.dto.SeriesDTO

@Component
class SeriesMapper : LibraryObjectMapperImpl<Series, SeriesDTO>() {
    override fun createDTO(): SeriesDTO = SeriesDTO()
}