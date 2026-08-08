package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.utils.ToStringUtils
import java.io.Serial

data class PlatformDTO(
    override var id: String? = null,
    override var name: String? = null,
    @param:JsonProperty("isRemoved")
    @get:JsonProperty("isRemoved")
    @field:JsonProperty("isRemoved")
    override var isRemoved: Boolean = false,
    var specificationId: String? = null,
    @param:JsonProperty("hasIcon")
    @get:JsonProperty("hasIcon")
    @field:JsonProperty("hasIcon")
    var hasIcon: Boolean = false,
    @param:JsonProperty("hasCoverImage")
    @get:JsonProperty("hasCoverImage")
    @field:JsonProperty("hasCoverImage")
    var hasCoverImage: Boolean = false,
    @param:JsonProperty("hasBackgroundImage")
    @get:JsonProperty("hasBackgroundImage")
    @field:JsonProperty("hasBackgroundImage")
    var hasBackgroundImage: Boolean = false
) : LibraryObjectDTO {

    fun withName(name: String) = copy(name = name)

    fun withRemoved(isRemoved: Boolean) = copy(isRemoved = isRemoved)

    override fun toString(): String {
        return ToStringUtils.createBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("isRemoved", isRemoved)
            .append("specificationId", specificationId)
            .append("hasIcon", hasIcon)
            .append("hasCoverImage", hasCoverImage)
            .append("hasBackgroundImage", hasBackgroundImage)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -2450022088290181433L
    }
}