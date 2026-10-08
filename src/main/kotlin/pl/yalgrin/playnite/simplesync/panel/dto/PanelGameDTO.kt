package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import pl.yalgrin.playnite.simplesync.library.dto.*
import java.io.Serial
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class PanelGameDTO(
    override var id: Long? = null,
    override var name: String? = null,
    var gameId: String? = null,
    var plugin: PanelLibraryPluginDTO? = null,
    var releaseDate: LocalDateTime? = null,
    var sortingName: String? = null,
    var series: List<PanelSeriesDTO> = emptyList(),
    var platforms: List<PanelPlatformDTO> = emptyList(),
    var ageRatings: List<PanelAgeRatingDTO> = emptyList(),
    var genres: List<PanelGenreDTO> = emptyList(),
    var regions: List<PanelRegionDTO> = emptyList(),
    var developers: List<CompanyDTO> = emptyList(),
    var source: SourceDTO? = null,
    var publishers: List<CompanyDTO> = emptyList(),
    var completionStatus: CompletionStatusDTO? = null,
    var categories: List<CategoryDTO> = emptyList(),
    var userScore: Int? = null,
    var features: List<FeatureDTO> = emptyList(),
    var criticScore: Int? = null,
    var tags: List<TagDTO> = emptyList(),
    var communityScore: Int? = null,
    var description: String? = null,
    var lastActivity: ZonedDateTime? = null,
    var playtime: Long = 0,
    var playCount: Long = 0,
    var version: String? = null,
    var manual: String? = null,
    @all:JsonProperty("isHidden")
    var isHidden: Boolean = false,
    @all:JsonProperty("isFavorite")
    var isFavorite: Boolean = false,
    var added: ZonedDateTime? = null,
    var modified: ZonedDateTime? = null,
    var notes: String? = null,
) : PanelObjectDTO {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 5629623178946260553L
    }

    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("id", id)
            .append("name", name)
            .toString()
    }
}

data class GameListDTO(
    val id: Long,
    val name: String?,
    @all:JsonProperty("isFavorite")
    val isFavorite: Boolean
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 5629623178946260553L
    }
}