package pl.yalgrin.playnite.simplesync.library.dto.filter

import java.io.Serial
import java.io.Serializable

data class IdItemPropertiesDTO(
    var ids: List<String> = emptyList(),
    var text: String? = null
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID = 3854104084958281184L
    }
}