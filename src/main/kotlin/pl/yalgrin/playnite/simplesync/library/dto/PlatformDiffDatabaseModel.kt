package pl.yalgrin.playnite.simplesync.library.dto

import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil
import java.io.Serial

data class PlatformDiffDatabaseModel(
    override var baseObjectId: Long? = null,
    override var changedFields: List<String> = emptyList(),
    var specificationId: String? = null
) : LibraryDiffDatabaseModelDTO {

    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("baseObjectId", baseObjectId)
            .append("changedFields", changedFields)
            .append("specificationId", specificationId)
            .toString()
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -6761100739460852828L
    }
}