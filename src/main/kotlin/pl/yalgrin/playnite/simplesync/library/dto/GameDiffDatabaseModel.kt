package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.library.enums.InstallSizeGroup
import pl.yalgrin.playnite.simplesync.library.enums.PlaytimeCategory
import pl.yalgrin.playnite.simplesync.library.enums.ScoreGroup
import java.io.Serial
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class GameDiffDatabaseModel(
    override var baseObjectId: Long? = null,
    override var changedFields: List<String> = emptyList(),
    var pluginId: String? = null,
    var pluginName: String? = null,
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
    var lastActivityInMs: Long? = null,
    var sortingName: String? = null,
    var platforms: List<PlatformDTO> = emptyList(),
    var publishers: List<CompanyDTO> = emptyList(),
    var developers: List<CompanyDTO> = emptyList(),
    var releaseDate: LocalDateTime? = null,
    var releaseYear: Int? = null,
    var categories: List<CategoryDTO> = emptyList(),
    var tags: List<TagDTO> = emptyList(),
    var features: List<FeatureDTO> = emptyList(),
    var links: List<LinkDTO> = emptyList(),
    var playtime: Long = 0,
    var playtimeCategory: PlaytimeCategory? = null,
    var playtimeDiff: Long? = null,
    var added: ZonedDateTime? = null,
    var addedInMs: Long? = null,
    var modified: ZonedDateTime? = null,
    var modifiedInMs: Long? = null,
    var playCount: Long = 0,
    var playCountDiff: Long? = null,
    var installSize: Long? = null,
    var installSizeGroup: InstallSizeGroup? = null,
    var lastSizeScanDate: ZonedDateTime? = null,
    var lastSizeScanDateInMs: Long? = null,
    var series: List<SeriesDTO> = emptyList(),
    var version: String? = null,
    var ageRatings: List<AgeRatingDTO> = emptyList(),
    var regions: List<RegionDTO> = emptyList(),
    var source: SourceDTO? = null,
    var completionStatus: CompletionStatusDTO? = null,
    var userScore: Int? = null,
    var userScoreGroup: ScoreGroup? = null,
    var criticScore: Int? = null,
    var criticScoreGroup: ScoreGroup? = null,
    var communityScore: Int? = null,
    var communityScoreGroup: ScoreGroup? = null,
    var manual: String? = null
) : LibraryDiffDatabaseModelDTO {

    companion object {
        @Serial
        private const val serialVersionUID = -5504426661125673615L
    }
}