package pl.yalgrin.playnite.simplesync.client.message

import pl.yalgrin.playnite.simplesync.client.enums.MessageType

abstract class ConnectionMessage(
    val messageType: MessageType
)
