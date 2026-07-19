package pl.yalgrin.playnite.simplesync.client.message

import pl.yalgrin.playnite.simplesync.client.enums.MessageType
import pl.yalgrin.playnite.simplesync.enums.ObjectType

data class ChangeMessage(
    var id: Long?,
    var type: ObjectType,
    var clientId: String?,
    var objectId: Long,
    var forceFetch: Boolean = false
) : ConnectionMessage(MessageType.CHANGE)
