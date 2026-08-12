package pl.yalgrin.playnite.simplesync.client.message

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.client.enums.MessageType
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType

data class ChangeMessage(
    var id: Long?,
    var type: ObjectType,
    var clientId: String? = null,
    var objectId: Long,
    @param:JsonProperty("isForceFetch")
    @get:JsonProperty("isForceFetch")
    @field:JsonProperty("isForceFetch")
    var isForceFetch: Boolean = false
) : ConnectionMessage(MessageType.CHANGE)
