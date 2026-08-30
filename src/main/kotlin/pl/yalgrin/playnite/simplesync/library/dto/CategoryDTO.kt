package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import java.io.Serial
import java.time.Instant

data class CategoryDTO(
    override var serverId: Long? = null,
    override var id: String? = null,
    override var name: String? = null,
    @param:JsonProperty("isRemoved")
    @get:JsonProperty("isRemoved")
    @field:JsonProperty("isRemoved")
    override var isRemoved: Boolean = false,
    override var createdAt: Instant? = null,
    override var createdBy: String? = null,
    override var modifiedAt: Instant? = null,
    override var modifiedBy: String? = null
) : LibraryObjectDTO {

    fun withName(name: String) = copy(name = name)

    fun withRemoved(isRemoved: Boolean) = copy(isRemoved = isRemoved)

    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("serverId", serverId)
            .append("id", id)
            .append("name", name)
            .append("isRemoved", isRemoved)
            .append("createdAt", createdAt)
            .append("createdBy", createdBy)
            .append("modifiedAt", modifiedAt)
            .append("modifiedBy", modifiedBy)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3512344634139844144L
    }
}