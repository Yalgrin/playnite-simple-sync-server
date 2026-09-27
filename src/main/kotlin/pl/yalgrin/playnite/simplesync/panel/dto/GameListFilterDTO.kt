package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.library.dto.filter.IdItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.IntItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.StringItemPropertiesDTO

data class GameListFilterDTO(
    @all:JsonProperty("useAndFilteringStyle")
    var useAndFilteringStyle: Boolean? = null,
    @all:JsonProperty("isInstalled")
    var isInstalled: Boolean? = null,
    @all:JsonProperty("isUninstalled")
    var isUninstalled: Boolean? = null,
    @all:JsonProperty("isHidden")
    var isHidden: Boolean? = null,
    @all:JsonProperty("isFavorite")
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