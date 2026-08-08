package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.utils.ToStringUtils
import java.io.Serial
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class GameDiffDTO(
    override var id: String? = null,
    override var name: String? = null,
    override var baseObjectId: Long? = null,
    override var changedFields: List<String> = emptyList(),
    @param:JsonProperty("isRemoved")
    @get:JsonProperty("isRemoved")
    @field:JsonProperty("isRemoved")
    override var isRemoved: Boolean = false,
    var description: String? = null,
    var notes: String? = null,
    var genres: List<GenreDTO> = emptyList(),
    @param:JsonProperty("isHidden")
    @get:JsonProperty("isHidden")
    @field:JsonProperty("isHidden")
    var isHidden: Boolean = false,
    @param:JsonProperty("isFavorite")
    @get:JsonProperty("isFavorite")
    @field:JsonProperty("isFavorite")
    var isFavorite: Boolean = false,
    var lastActivity: ZonedDateTime? = null,
    var sortingName: String? = null,
    var gameId: String? = null,
    var pluginId: String? = null,
    var platforms: List<PlatformDTO> = emptyList(),
    var publishers: List<CompanyDTO> = emptyList(),
    var developers: List<CompanyDTO> = emptyList(),
    var releaseDate: LocalDateTime? = null,
    var categories: List<CategoryDTO> = emptyList(),
    var tags: List<TagDTO> = emptyList(),
    var features: List<FeatureDTO> = emptyList(),
    var links: List<LinkDTO> = emptyList(),
    var playtime: Long = 0,
    var playtimeDiff: Long? = null,
    var added: ZonedDateTime? = null,
    var modified: ZonedDateTime? = null,
    var playCount: Long = 0,
    var playCountDiff: Long? = null,
    var installSize: Long? = null,
    var lastSizeScanDate: ZonedDateTime? = null,
    var series: List<SeriesDTO> = emptyList(),
    var version: String? = null,
    var ageRatings: List<AgeRatingDTO> = emptyList(),
    var regions: List<RegionDTO> = emptyList(),
    var source: SourceDTO? = null,
    var completionStatus: CompletionStatusDTO? = null,
    var userScore: Int? = null,
    var criticScore: Int? = null,
    var communityScore: Int? = null,
    var manual: String? = null
) : LibraryObjectDiffDTO {

    override fun toString(): String {
        return ToStringUtils.createBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("isRemoved", isRemoved)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID = -5504426661125673615L
    }
}

class GameDiffFields {
    companion object {
        const val ID: String = "Id"
        const val NAME: String = "Name"
        const val REMOVED: String = "Removed"
        const val DESCRIPTION: String = "Description"
        const val NOTES: String = "Notes"
        const val GENRES: String = "Genres"
        const val HIDDEN: String = "Hidden"
        const val FAVORITE: String = "Favorite"
        const val LAST_ACTIVITY: String = "LastActivity"
        const val SORTING_NAME: String = "SortingName"
        const val GAME_ID: String = "GameId"
        const val PLUGIN_ID: String = "PluginId"
        const val PLATFORMS: String = "Platforms"
        const val PUBLISHERS: String = "Publishers"
        const val DEVELOPERS: String = "Developers"
        const val RELEASE_DATE: String = "ReleaseDate"
        const val CATEGORIES: String = "Categories"
        const val TAGS: String = "Tags"
        const val FEATURES: String = "Features"
        const val LINKS: String = "Links"
        const val PLAYTIME: String = "Playtime"
        const val PLAYTIME_DIFF: String = "PlaytimeDiff"
        const val ADDED: String = "Added"
        const val MODIFIED: String = "Modified"
        const val PLAY_COUNT: String = "PlayCount"
        const val PLAY_COUNT_DIFF: String = "PlayCountDiff"
        const val INSTALL_SIZE: String = "InstallSize"
        const val LAST_SIZE_SCAN_DATE: String = "LastSizeScanDate"
        const val SERIES: String = "Series"
        const val VERSION: String = "Version"
        const val AGE_RATINGS: String = "AgeRatings"
        const val REGIONS: String = "Regions"
        const val SOURCE: String = "Source"
        const val COMPLETION_STATUS: String = "CompletionStatus"
        const val USER_SCORE: String = "UserScore"
        const val CRITIC_SCORE: String = "CriticScore"
        const val COMMUNITY_SCORE: String = "CommunityScore"
        const val MANUAL: String = "Manual"
    }
}