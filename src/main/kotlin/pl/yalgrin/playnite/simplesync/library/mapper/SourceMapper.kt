package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Source
import pl.yalgrin.playnite.simplesync.library.dto.SourceDTO

@Component
class SourceMapper : LibraryObjectMapperImpl<Source, SourceDTO>() {
    override fun createDTO(): SourceDTO = SourceDTO()
}