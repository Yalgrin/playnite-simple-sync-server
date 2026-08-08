package pl.yalgrin.playnite.simplesync.library.dto

import com.fasterxml.jackson.annotation.JsonProperty
import pl.yalgrin.playnite.simplesync.common.utils.ToStringUtils
import java.io.Serial

data class SourceDTO(
    override var id: String? = null,
    override var name: String? = null,
    @param:JsonProperty("isRemoved")
    @get:JsonProperty("isRemoved")
    @field:JsonProperty("isRemoved")
    override var isRemoved: Boolean = false
) : LibraryObjectDTO {

    fun withName(name: String) = copy(name = name)

    fun withRemoved(isRemoved: Boolean) = copy(isRemoved = isRemoved)

    override fun toString(): String {
        return ToStringUtils.createBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("isRemoved", isRemoved)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 394166563735763497L
    }
}