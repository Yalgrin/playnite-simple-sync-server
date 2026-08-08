package pl.yalgrin.playnite.simplesync.library.dto

import java.io.Serializable

interface LibraryObjectDiffDTO : Serializable {
    var id: String?
    var name: String?
    var baseObjectId: Long?
    var changedFields: List<String>
    var isRemoved: Boolean
}