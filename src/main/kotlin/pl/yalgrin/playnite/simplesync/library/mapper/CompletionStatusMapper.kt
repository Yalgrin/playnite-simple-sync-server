package pl.yalgrin.playnite.simplesync.library.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.CompletionStatus
import pl.yalgrin.playnite.simplesync.library.dto.CompletionStatusDTO

@Component
class CompletionStatusMapper : LibraryObjectMapperImpl<CompletionStatus, CompletionStatusDTO>() {
    override fun createDTO(): CompletionStatusDTO = CompletionStatusDTO()
}