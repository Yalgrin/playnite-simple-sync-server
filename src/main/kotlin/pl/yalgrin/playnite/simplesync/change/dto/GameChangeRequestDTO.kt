package pl.yalgrin.playnite.simplesync.change.dto

import pl.yalgrin.playnite.simplesync.common.dto.AbstractDTO
import java.io.Serial

data class GameChangeRequestDTO(
    var ids: List<String> = emptyList(),
    var gameIds: List<GameIdsDTO> = emptyList()
) : AbstractDTO() {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1610779580164593544L
    }
}