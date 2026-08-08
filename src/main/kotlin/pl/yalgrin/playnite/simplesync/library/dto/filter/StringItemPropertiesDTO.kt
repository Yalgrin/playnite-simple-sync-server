package pl.yalgrin.playnite.simplesync.library.dto.filter

import java.io.Serial
import java.io.Serializable

data class StringItemPropertiesDTO(
    var values: List<String> = emptyList()
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID = 1808659491232511772L
    }
}