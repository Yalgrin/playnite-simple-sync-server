package pl.yalgrin.playnite.simplesync.panel.dto

import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import java.io.Serial

data class PanelGenreDTO(
    override var id: Long? = null,
    override var name: String? = null
) : PanelObjectDTO {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -8362917405284619374L
    }
}
