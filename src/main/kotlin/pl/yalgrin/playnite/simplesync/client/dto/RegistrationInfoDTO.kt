package pl.yalgrin.playnite.simplesync.client.dto

data class RegistrationInfoDTO(
    val registrationSpecified: Boolean,
    val registrationValid: Boolean,
    val displayClientName: String,
    val sessionActive: Boolean
)