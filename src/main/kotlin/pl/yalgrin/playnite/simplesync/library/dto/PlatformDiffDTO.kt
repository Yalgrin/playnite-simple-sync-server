package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.utils.ToStringUtils
import java.io.Serial

data class PlatformDiffDTO(
    override var id: String? = null,
    override var name: String? = null,
    override var baseObjectId: Long? = null,
    override var changedFields: List<String> = emptyList(),
    @param:JsonProperty("isRemoved")
    @get:JsonProperty("isRemoved")
    @field:JsonProperty("isRemoved")
    override var isRemoved: Boolean = false,
    var specificationId: String? = null
) : LibraryObjectDiffDTO {

    override fun toString(): String {
        return ToStringUtils.createBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("baseObjectId", baseObjectId)
            .append("changedFields", changedFields)
            .append("isRemoved", isRemoved)
            .append("specificationId", specificationId)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -6761100739460852828L
    }
}