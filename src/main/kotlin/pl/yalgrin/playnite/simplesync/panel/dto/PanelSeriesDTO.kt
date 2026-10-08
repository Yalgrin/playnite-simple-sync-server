package pl.yalgrin.playnite.simplesync.panel.dto

import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import java.io.Serial

data class PanelSeriesDTO(
    override var id: Long? = null,
    override var name: String? = null
) : PanelObjectDTO {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -5284719362048571293L
    }
}
