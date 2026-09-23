package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.library.dto.filter.IdItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.IntItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.StringItemPropertiesDTO

data class GameListFilterDTO(
    @param:JsonProperty("useAndFilteringStyle")
    @get:JsonProperty("useAndFilteringStyle")
    @field:JsonProperty("useAndFilteringStyle")
    var useAndFilteringStyle: Boolean? = null,
    @param:JsonProperty("isInstalled")
    @get:JsonProperty("isInstalled")
    @field:JsonProperty("isInstalled")
    var isInstalled: Boolean? = null,
    @param:JsonProperty("isUninstalled")
    @get:JsonProperty("isUninstalled")
    @field:JsonProperty("isUninstalled")
    var isUninstalled: Boolean? = null,
    @param:JsonProperty("isHidden")
    @get:JsonProperty("isHidden")
    @field:JsonProperty("isHidden")
    var isHidden: Boolean? = null,
    @param:JsonProperty("isFavorite")
    @get:JsonProperty("isFavorite")
    @field:JsonProperty("isFavorite")
    var isFavorite: Boolean? = null,
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
    var installSize: IntItemPropertiesDTO? = null,

    val sortingOrder: String? = null,
    val sortingOrderDirection: String? = null,
    val groupingOrder: String? = null,
)