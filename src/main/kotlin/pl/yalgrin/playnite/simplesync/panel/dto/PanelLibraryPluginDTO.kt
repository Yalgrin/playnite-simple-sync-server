package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import java.io.Serial

data class PanelLibraryPluginDTO(
    override var id: Long? = null,
    override var name: String? = null,
    @all:JsonProperty("hasIcon")
    var hasIcon: Boolean = false,
    @all:JsonProperty("hasBackgroundImage")
    var hasBackgroundImage: Boolean = false,
    var iconHash: String? = null,
    var backgroundImageHash: String? = null,
    @all:JsonProperty("isSavingIcon")
    var isSavingIcon: Boolean = false,
    @all:JsonProperty("isSavingBackground")
    var isSavingBackground: Boolean = false
) : PanelObjectDTO {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -6328107271286519392L
    }
}