package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Tag
import pl.yalgrin.playnite.simplesync.library.dto.TagDTO

@Component
class TagMapper : LibraryObjectMapperImpl<Tag, TagDTO>() {
    override fun createDTO(): TagDTO = TagDTO()
}