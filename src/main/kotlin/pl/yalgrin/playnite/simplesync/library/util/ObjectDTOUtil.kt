package pl.yalgrin.playnite.simplesync.library.util

import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO

fun <T : LibraryObjectDTO> T.clearModificationData(): T {
    createdAt = null
    createdBy = null
    modifiedAt = null
    modifiedBy = null
    return this
}

fun <T : LibraryObjectDTO> List<T>.clearModificationData(): List<T> {
    forEach { it.clearModificationData() }
    return this
}