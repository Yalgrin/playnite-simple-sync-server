package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import java.io.Serial
import java.time.Instant

data class PlatformDiffDTO(
    override var serverId: Long? = null,
    override var id: String? = null,
    override var name: String? = null,
    override var baseObjectId: Long? = null,
    override var changedFields: List<String> = emptyList(),
    @param:JsonProperty("isRemoved")
    @get:JsonProperty("isRemoved")
    @field:JsonProperty("isRemoved")
    override var isRemoved: Boolean = false,
    var specificationId: String? = null,
    override var createdAt: Instant? = null,
    override var createdBy: String? = null
) : LibraryObjectDiffDTO {

    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("serverId", serverId)
            .append("id", id)
            .append("name", name)
            .append("baseObjectId", baseObjectId)
            .append("changedFields", changedFields)
            .append("isRemoved", isRemoved)
            .append("specificationId", specificationId)
            .append("createdAt", createdAt)
            .append("createdBy", createdBy)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -6761100739460852828L
    }
}