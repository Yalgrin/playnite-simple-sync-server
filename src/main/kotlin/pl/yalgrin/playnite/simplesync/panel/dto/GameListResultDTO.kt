package pl.yalgrin.playnite.simplesync.panel.dto

import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import java.io.Serial
import java.io.Serializable

data class GameListResultDTO(
    val ungroupedResults: List<GameListDTO> = emptyList(),
    val sortedGroups: List<String> = emptyList(),
    val groupingOrder: String? = null,
    val groupedResults: Map<String, List<GameListDTO>> = emptyMap()
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -1770754161125110989L
    }

    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("ungroupedResults.size()", ungroupedResults.size)
            .append("sortedGroups.size()", sortedGroups.size)
            .append("groupedResults.size()", groupedResults.size)
            .build()
    }
}