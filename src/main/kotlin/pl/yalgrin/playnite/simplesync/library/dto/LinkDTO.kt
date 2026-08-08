package pl.yalgrin.playnite.simplesync.library.dto

import java.io.Serial
import java.io.Serializable

data class LinkDTO(
    var name: String? = null,
    var url: String? = null
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID = 5001751673402015078L
    }
}