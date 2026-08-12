package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Genre
import pl.yalgrin.playnite.simplesync.library.dto.GenreDTO

@Component
class GenreMapper : LibraryObjectMapperImpl<Genre, GenreDTO>() {
    override fun createDTO(): GenreDTO = GenreDTO()
}