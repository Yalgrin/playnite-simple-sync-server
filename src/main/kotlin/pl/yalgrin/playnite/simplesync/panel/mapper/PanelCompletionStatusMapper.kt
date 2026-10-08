package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.CompletionStatus
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCompletionStatusDTO

@Component
class PanelCompletionStatusMapper : PanelObjectMapperImpl<CompletionStatus, PanelCompletionStatusDTO>() {
    override fun createDTO(): PanelCompletionStatusDTO = PanelCompletionStatusDTO()
}
