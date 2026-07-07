package pl.yalgrin.playnite.simplesync.client.message

import pl.yalgrin.playnite.simplesync.client.enums.MessageType

data class InitializationMessage(
    val sessionId: String
) : ConnectionMessage(MessageType.INITIALIZATION)
