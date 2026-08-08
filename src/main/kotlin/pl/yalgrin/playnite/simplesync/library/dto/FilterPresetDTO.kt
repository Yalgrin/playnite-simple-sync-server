package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.utils.ToStringUtils
import pl.yalgrin.playnite.simplesync.library.dto.filter.FilterPresetSettingsDTO
import java.io.Serial

data class FilterPresetDTO(
    override var id: String? = null,
    override var name: String? = null,
    @param:JsonProperty("isRemoved")
    @get:JsonProperty("isRemoved")
    @field:JsonProperty("isRemoved")
    override var isRemoved: Boolean = false,
    var settings: FilterPresetSettingsDTO? = null,
    var sortingOrder: String? = null,
    var sortingOrderDirection: String? = null,
    var groupingOrder: String? = null,
    @param:JsonProperty("showInFullscreenQuickSelection")
    @get:JsonProperty("showInFullscreenQuickSelection")
    @field:JsonProperty("showInFullscreenQuickSelection")
    var showInFullscreenQuickSelection: Boolean = false
) : LibraryObjectDTO {

    fun withName(name: String) = copy(name = name)

    fun withRemoved(isRemoved: Boolean) = copy(isRemoved = isRemoved)

    override fun toString(): String {
        return ToStringUtils.createBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("isRemoved", isRemoved)
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