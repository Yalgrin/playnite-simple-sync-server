package pl.yalgrin.playnite.simplesync.panel.dto

import java.io.Serial
import java.io.Serializable

data class GameListResultDTO(
    val ungroupedResults: List<GameListDTO> = emptyList(),
    val groupedResults: Map<String, List<GameListDTO>> = emptyMap()
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -1770754161125110989L
    }
}