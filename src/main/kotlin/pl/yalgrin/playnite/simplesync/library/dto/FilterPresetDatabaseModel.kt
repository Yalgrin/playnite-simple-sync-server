package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.library.dto.filter.FilterPresetSettingsDTO
import java.io.Serial
import java.io.Serializable

data class FilterPresetDatabaseModel(
    var settings: FilterPresetSettingsDTO? = null,
    var sortingOrder: String? = null,
    var sortingOrderDirection: String? = null,
    var groupingOrder: String? = null,
    @param:JsonProperty("showInFullscreenQuickSelection")
    @get:JsonProperty("showInFullscreenQuickSelection")
    @field:JsonProperty("showInFullscreenQuickSelection")
    var showInFullscreenQuickSelection: Boolean = false
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -6526930157707316885L
    }
}