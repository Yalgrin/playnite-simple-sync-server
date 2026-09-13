package pl.yalgrin.playnite.simplesync.library.dto

import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import java.io.Serial

data class LibraryPluginDiffDatabaseModel(
    override var baseObjectId: Long? = null,
    override var changedFields: List<String> = emptyList()
) : LibraryDiffDatabaseModelDTO {

    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("baseObjectId", baseObjectId)
            .append("changedFields", changedFields)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1658716026705973780L
    }
}