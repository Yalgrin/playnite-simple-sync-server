package pl.yalgrin.playnite.simplesync.library.dto.filter

import java.io.Serial
import java.io.Serializable

data class IntItemPropertiesDTO(
    var values: List<Int> = emptyList()
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID = 6747197307373352936L
    }
}