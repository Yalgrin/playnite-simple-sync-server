package pl.yalgrin.playnite.simplesync.common.util

import org.apache.commons.lang3.Strings
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO
import pl.yalgrin.playnite.simplesync.library.dto.LinkDTO

object MapperUtil {
    fun hasChanged(obj1: Long?, obj2: Long?): Boolean {
        return (obj1 ?: 0L) != (obj2 ?: 0L)
    }

    fun hasChanged(obj1: String?, obj2: String?): Boolean {
        return !Strings.CS.equals(obj1, obj2)
    }

    fun hasChanged(obj1: Any?, obj2: Any?): Boolean {
        return obj1 != obj2
    }

    fun hasChanged(
        oldList: List<LibraryObjectDTO>?,
        newList: List<LibraryObjectDTO>?
    ): Boolean {
        val oldIds = oldList?.mapNotNull { it.id }?.toSet().orEmpty()
        val newIds = newList?.mapNotNull { it.id }?.toSet().orEmpty()
        return oldIds.size != newIds.size || oldIds.any { it !in newIds }
    }

    fun haveLinksChanged(oldList: List<LinkDTO>?, newList: List<LinkDTO>?): Boolean {
        val oldLinks = oldList.orEmpty()
        val newLinks = newList.orEmpty()
        return oldLinks.size != newLinks.size || oldLinks.indices.any { i ->
            val oldLink = oldLinks[i]
            val newLink = newLinks[i]
            !Strings.CS.equals(oldLink.url, newLink.url) || !Strings.CS.equals(oldLink.name, newLink.name)
        }
    }
}