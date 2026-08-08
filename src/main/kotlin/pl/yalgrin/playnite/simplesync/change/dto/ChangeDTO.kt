package pl.yalgrin.playnite.simplesync.change.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.dto.AbstractDTO
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.common.utils.ToStringUtils
import java.io.Serial

data class ChangeDTO(
    var id: Long? = null,
    var type: ObjectType? = null,
    var clientId: String? = null,
    var objectId: Long? = null,
    @param:JsonProperty("isForceFetch")
    @get:JsonProperty("isForceFetch")
    @field:JsonProperty("isForceFetch")
    var isForceFetch: Boolean = false
) : AbstractDTO() {

    override fun toString(): String {
        return ToStringUtils.createBuilder(this)
            .append("id", id)
            .append("type", type)
            .append("clientId", clientId)
            .append("objectId", objectId)
            .append("isForceFetch", isForceFetch)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID = 6362405151054072827L
    }
}