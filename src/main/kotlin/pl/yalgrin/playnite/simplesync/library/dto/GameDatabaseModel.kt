package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.config.json.AnyFormatLocalDateTimeDeserializer
import pl.yalgrin.playnite.simplesync.common.config.json.AnyFormatZonedDateTimeDeserializer
import pl.yalgrin.playnite.simplesync.library.enums.InstallSizeGroup
import pl.yalgrin.playnite.simplesync.library.enums.PlaytimeCategory
import pl.yalgrin.playnite.simplesync.library.enums.ScoreGroup
import tools.jackson.databind.annotation.JsonDeserialize
import java.io.Serial
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class GameDatabaseModel(
    var pluginId: String? = null,
    var pluginName: String? = null,
    var description: String? = null,
    var notes: String? = null,
    var genres: List<GenreDTO> = emptyList(),
    @all:JsonProperty("isHidden")
    var isHidden: Boolean = false,
    @all:JsonProperty("isFavorite")
    var isFavorite: Boolean = false,
    @all:JsonDeserialize(using = AnyFormatZonedDateTimeDeserializer::class)
    var lastActivity: ZonedDateTime? = null,
    var lastActivityInMs: Long? = null,
    @all:JsonDeserialize(using = AnyFormatZonedDateTimeDeserializer::class)
    var recentActivity: ZonedDateTime? = null,
    var recentActivityInMs: Long? = null,
    var sortingName: String? = null,
    var platforms: List<PlatformDTO> = emptyList(),
    var publishers: List<CompanyDTO> = emptyList(),
    var developers: List<CompanyDTO> = emptyList(),
    @all:JsonDeserialize(using = AnyFormatLocalDateTimeDeserializer::class)
    var releaseDate: LocalDateTime? = null,
    var releaseYear: Int? = null,
    var categories: List<CategoryDTO> = emptyList(),
    var tags: List<TagDTO> = emptyList(),
    var features: List<FeatureDTO> = emptyList(),
    var links: List<LinkDTO> = emptyList(),
    var playtime: Long = 0,
    var playtimeCategory: PlaytimeCategory = PlaytimeCategory.NOT_PLAYED,
    @all:JsonDeserialize(using = AnyFormatZonedDateTimeDeserializer::class)
    var added: ZonedDateTime? = null,
    var addedInMs: Long? = null,
    @all:JsonDeserialize(using = AnyFormatZonedDateTimeDeserializer::class)
    var modified: ZonedDateTime? = null,
    var modifiedInMs: Long? = null,
    var playCount: Long = 0,
    var installSize: Long? = null,
    var installSizeGroup: InstallSizeGroup = InstallSizeGroup.NONE,
    @all:JsonDeserialize(using = AnyFormatZonedDateTimeDeserializer::class)
    var lastSizeScanDate: ZonedDateTime? = null,
    var lastSizeScanDateInMs: Long? = null,
    var series: List<SeriesDTO> = emptyList(),
    var version: String? = null,
    var ageRatings: List<AgeRatingDTO> = emptyList(),
    var regions: List<RegionDTO> = emptyList(),
    var source: SourceDTO? = null,
    var completionStatus: CompletionStatusDTO? = null,
    var userScore: Int? = null,
    var userScoreGroup: ScoreGroup = ScoreGroup.NONE,
    var criticScore: Int? = null,
    var criticScoreGroup: ScoreGroup = ScoreGroup.NONE,
    var communityScore: Int? = null,
    var communityScoreGroup: ScoreGroup = ScoreGroup.NONE,
    var manual: String? = null
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 4371203767392750611L
    }

}