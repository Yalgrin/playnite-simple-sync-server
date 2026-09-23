package pl.yalgrin.playnite.simplesync.library.dto.filter

import java.io.Serial
import java.io.Serializable

data class EnumItemPropertiesDTO<T>(
    var values: List<T> = emptyList()
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID = 1808659491232511772L
    }
}