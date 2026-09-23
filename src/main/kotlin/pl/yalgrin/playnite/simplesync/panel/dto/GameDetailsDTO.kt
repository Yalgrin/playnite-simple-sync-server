package pl.yalgrin.playnite.simplesync.panel.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import java.io.Serial
import java.io.Serializable

data class GameDetailsDTO(
    val id: Long,
    val name: String,
    val description: String?
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 5629623178946260553L
    }

    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("id", id)
            .append("name", name)
            .toString()
    }
}

data class GameListDTO(
    val id: Long,
    val name: String?,
    @param:JsonProperty("isFavorite")
    @get:JsonProperty("isFavorite")
    @field:JsonProperty("isFavorite")
    val isFavorite: Boolean
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 5629623178946260553L
    }
}