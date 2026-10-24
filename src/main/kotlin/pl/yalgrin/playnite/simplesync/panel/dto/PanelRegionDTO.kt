package pl.yalgrin.playnite.simplesync.panel.dto

import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import java.io.Serial

data class PanelRegionDTO(
    override var id: Long? = null,
    override var playniteId: String? = null,
    override var name: String? = null,
    var specificationId: String? = null
) : PanelObjectDTO {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -2865471930486512734L
    }
}
