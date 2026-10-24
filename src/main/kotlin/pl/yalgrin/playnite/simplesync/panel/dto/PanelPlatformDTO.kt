package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import java.io.Serial

data class PanelPlatformDTO(
    override var id: Long? = null,
    override var playniteId: String? = null,
    override var name: String? = null,
    var specificationId: String? = null,
    var iconUrl: String? = null,
    var coverImageUrl: String? = null,
    var backgroundImageUrl: String? = null,
    @all:JsonProperty("isSavingIcon")
    var isSavingIcon: Boolean = false,
    @all:JsonProperty("isSavingCoverImage")
    var isSavingCoverImage: Boolean = false,
    @all:JsonProperty("isSavingBackground")
    var isSavingBackground: Boolean = false
) : PanelObjectDTO {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -2865471930486512734L
    }
}
