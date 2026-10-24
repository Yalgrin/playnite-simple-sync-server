package pl.yalgrin.playnite.simplesync.client.dto

data class SessionInfoDTO(
    val clientId: String,
    val displayName: String,
    val sessionId: String,
    val panelUser: Boolean = false
)

data class SessionSettingsDTO(
    val enabledChangeStream: Boolean = false
)