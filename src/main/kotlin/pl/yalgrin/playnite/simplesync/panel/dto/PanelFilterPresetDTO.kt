package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.FilterPresetSettingsDTO
import java.io.Serial

data class PanelFilterPresetDTO(
    override var id: Long? = null,
    override var name: String? = null,
    var settings: FilterPresetSettingsDTO? = null,
    var sortingOrder: String? = null,
    var sortingOrderDirection: String? = null,
    var groupingOrder: String? = null,
    @all:JsonProperty("showInFullscreenQuickSelection")
    var showInFullscreenQuickSelection: Boolean = false
) : PanelObjectDTO {

    fun withName(name: String) = copy(name = name)

    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("settings", settings)
            .append("sortingOrder", sortingOrder)
            .append("sortingOrderDirection", sortingOrderDirection)
            .append("groupingOrder", groupingOrder)
            .append("showInFullscreenQuickSelection", showInFullscreenQuickSelection)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3449683227490841780L
    }
}