package pl.yalgrin.playnite.simplesync.dto

data class ErrorDTO(
    val message: String? = null,
    val fieldErrors: List<FieldErrorDTO>? = null
)

data class FieldErrorDTO(
    val field: String? = null,
    val message: String? = null
)