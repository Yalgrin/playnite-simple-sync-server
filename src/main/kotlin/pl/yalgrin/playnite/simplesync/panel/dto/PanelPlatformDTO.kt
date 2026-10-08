package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import java.io.Serial

data class PanelPlatformDTO(
    override var id: Long? = null,
    override var name: String? = null,
    var specificationId: String? = null,
    @all:JsonProperty("hasIcon")
    var hasIcon: Boolean = false,
    @all:JsonProperty("hasCoverImage")
    var hasCoverImage: Boolean = false,
    @all:JsonProperty("hasBackgroundImage")
    var hasBackgroundImage: Boolean = false,
) : PanelObjectDTO {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -2865471930486512734L
    }
}
