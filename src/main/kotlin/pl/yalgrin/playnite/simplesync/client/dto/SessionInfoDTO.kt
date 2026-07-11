package pl.yalgrin.playnite.simplesync.client.dto

data class SessionInfoDTO(
    val clientId: String,
    val displayName: String,
    val sessionId: String
)

data class SessionSettingsDTO(
    val enabledChangeStream: Boolean = false
)