package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import java.io.Serial
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class GameDiffDatabaseModel(
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
        return ToStringUtil.createBuilder(this)
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