package pl.yalgrin.playnite.simplesync.client.dto

data class RegisteredClientDTO(
    val clientId: String,
    val displayName: String,
    val clientToken: String
) {
    override fun toString(): String {
        return "RegisteredClientDTO(displayName='$displayName')"
    }
}
