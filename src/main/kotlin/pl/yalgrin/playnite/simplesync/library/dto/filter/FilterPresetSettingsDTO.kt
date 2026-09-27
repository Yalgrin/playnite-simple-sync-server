package pl.yalgrin.playnite.simplesync.library.dto.filter

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serial
import java.io.Serializable

data class FilterPresetSettingsDTO(
    @all:JsonProperty("useAndFilteringStyle")
    var useAndFilteringStyle: Boolean = false,
    @all:JsonProperty("isInstalled")
    var isInstalled: Boolean = false,
    @all:JsonProperty("isUninstalled")
    var isUninstalled: Boolean = false,
    @all:JsonProperty("isHidden")
    var isHidden: Boolean = false,
    @all:JsonProperty("isFavorite")
    var isFavorite: Boolean = false,
    var name: String? = null,
    var version: String? = null,

    var releaseYear: StringItemPropertiesDTO? = null,

    var genre: IdItemPropertiesDTO? = null,
    var platform: IdItemPropertiesDTO? = null,
    var publisher: IdItemPropertiesDTO? = null,
    var developer: IdItemPropertiesDTO? = null,
    var category: IdItemPropertiesDTO? = null,
    var tag: IdItemPropertiesDTO? = null,
    var series: IdItemPropertiesDTO? = null,
    var region: IdItemPropertiesDTO? = null,
    var source: IdItemPropertiesDTO? = null,
    var ageRating: IdItemPropertiesDTO? = null,
    var library: IdItemPropertiesDTO? = null,
    var completionStatuses: IdItemPropertiesDTO? = null,
    var feature: IdItemPropertiesDTO? = null,

    var userScore: IntItemPropertiesDTO? = null,
    var criticScore: IntItemPropertiesDTO? = null,
    var communityScore: IntItemPropertiesDTO? = null,
    var lastActivity: IntItemPropertiesDTO? = null,
    var recentActivity: IntItemPropertiesDTO? = null,
    var added: IntItemPropertiesDTO? = null,
    var modified: IntItemPropertiesDTO? = null,
    var playTime: IntItemPropertiesDTO? = null,
    var installSize: IntItemPropertiesDTO? = null
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID = 1511413271560442002L
    }
}