package pl.yalgrin.playnite.simplesync.library.dto

import java.io.Serializable

interface LibraryDiffDatabaseModelDTO : Serializable {
    var baseObjectId: Long?
    var changedFields: List<String>
}