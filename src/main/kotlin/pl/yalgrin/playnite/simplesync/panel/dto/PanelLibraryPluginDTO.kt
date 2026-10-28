package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import java.io.Serial

data class PanelLibraryPluginDTO(
    override var id: Long? = null,
    override var playniteId: String? = null,
    override var name: String? = null,
    var iconUrl: String? = null,
    var backgroundImageUrl: String? = null,
    @all:JsonProperty("isSavingIcon")
    var isSavingIcon: Boolean = false,
    @all:JsonProperty("isSavingBackgroundImage")
    var isSavingBackgroundImage: Boolean = false
) : PanelObjectDTO {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -6328107271286519392L
    }
}