package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serial
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class GameDatabaseModel(
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
    var added: ZonedDateTime? = null,
    var modified: ZonedDateTime? = null,
    var playCount: Long = 0,
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
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 4371203767392750611L
    }

}