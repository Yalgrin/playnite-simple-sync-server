package pl.yalgrin.playnite.simplesync.client.message

import pl.yalgrin.playnite.simplesync.client.enums.MessageType

data class InitializationMessage(
    val sessionId: String,
    val serverVersion: String?
) : ConnectionMessage(MessageType.INITIALIZATION)
