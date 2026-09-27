package pl.yalgrin.playnite.simplesync.library.service

import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.common.util.ToStringUtil

data class LibrarySaveResult<T>(
    val savedObject: T,
    val generatedChanges: List<ChangeDTO>
) {
    override fun toString(): String {
        return ToStringUtil.createBuilder(this)
            .append("savedObject", savedObject)
            .append("generatedChanges.size()", generatedChanges.size)
            .toString()
    }
}