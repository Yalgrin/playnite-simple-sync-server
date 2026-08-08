package pl.yalgrin.playnite.simplesync.change.dto

import pl.yalgrin.playnite.simplesync.common.dto.AbstractDTO
import java.io.Serial

data class GameIdsDTO(
    var gameId: String,
    var pluginId: String
) : AbstractDTO() {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 6037277647560770671L
    }
}