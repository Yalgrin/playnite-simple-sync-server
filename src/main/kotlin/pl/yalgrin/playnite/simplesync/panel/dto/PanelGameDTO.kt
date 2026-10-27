package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.config.json.AnyFormatLocalDateTimeDeserializer
import pl.yalgrin.playnite.simplesync.common.config.json.AnyFormatZonedDateTimeDeserializer
import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import pl.yalgrin.playnite.simplesync.library.dto.LinkDTO
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import tools.jackson.databind.annotation.JsonDeserialize
import java.io.Serial
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class PanelGameDTO(
    override var id: Long? = null,
    override var playniteId: String? = null,
    override var name: String? = null,
    var gameId: String? = null,
    var plugin: PanelLibraryPluginDTO? = null,
    @all:JsonDeserialize(using = AnyFormatLocalDateTimeDeserializer::class)
    var releaseDate: LocalDateTime? = null,
    var sortingName: String? = null,
    var series: List<PanelSeriesDTO> = emptyList(),
    var platforms: List<PanelPlatformDTO> = emptyList(),
    var ageRatings: List<PanelAgeRatingDTO> = emptyList(),
    var genres: List<PanelGenreDTO> = emptyList(),
    var regions: List<PanelRegionDTO> = emptyList(),
    var developers: List<PanelCompanyDTO> = emptyList(),
    var source: PanelSourceDTO? = null,
    var publishers: List<PanelCompanyDTO> = emptyList(),
    var completionStatus: PanelCompletionStatusDTO? = null,
    var categories: List<PanelCategoryDTO> = emptyList(),
    var userScore: Int? = null,
    var features: List<PanelFeatureDTO> = emptyList(),
    var criticScore: Int? = null,
    var tags: List<PanelTagDTO> = emptyList(),
    var communityScore: Int? = null,
    var description: String? = null,
    @all:JsonDeserialize(using = AnyFormatZonedDateTimeDeserializer::class)
    var lastActivity: ZonedDateTime? = null,
    var playtime: Long = 0,
    var playCount: Long = 0,
    var installSize: Long? = null,
    var version: String? = null,
    var manual: String? = null,
    @all:JsonProperty("isHidden")
    var isHidden: Boolean = false,
    @all:JsonProperty("isFavorite")
    var isFavorite: Boolean = false,
    @all:JsonDeserialize(using = AnyFormatZonedDateTimeDeserializer::class)
    var added: ZonedDateTime? = null,
    @all:JsonDeserialize(using = AnyFormatZonedDateTimeDeserializer::class)
    var modified: ZonedDateTime? = null,
    var notes: String? = null,
    var links: List<LinkDTO> = emptyList(),
    var iconUrl: String? = null,
    var coverImageUrl: String? = null,
    var backgroundImageUrl: String? = null,
    @all:JsonProperty("isSavingIcon")
    var isSavingIcon: Boolean = false,
    @all:JsonProperty("isSavingCoverImage")
    var isSavingCoverImage: Boolean = false,
    @all:JsonProperty("isSavingBackground")
    var isSavingBackground: Boolean = false
) : PanelObjectDTO {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 5629623178946260553L
    }

    fun copyAll() = copy()

    fun withName(name: String?) = copy(name = name)

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
    var plugin: PanelLibraryPluginDTO? = null,
    var completionStatus: PanelCompletionStatusDTO? = null,
    @all:JsonDeserialize(using = AnyFormatZonedDateTimeDeserializer::class)
    var lastActivity: ZonedDateTime? = null,
    var playtime: Long = 0,
    @all:JsonProperty("isFavorite")
    val isFavorite: Boolean,
    @all:JsonProperty("hasIcon")
    var hasIcon: Boolean = false,
    var iconUrl: String? = null,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 5629623178946260553L
    }
}