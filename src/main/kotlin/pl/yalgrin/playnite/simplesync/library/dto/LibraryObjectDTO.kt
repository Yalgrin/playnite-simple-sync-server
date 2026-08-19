package pl.yalgrin.playnite.simplesync.library.dto

import java.io.Serializable

interface LibraryObjectDTO : Serializable {
    var externalId: Long?
    var id: String?
    var name: String?
    var isRemoved: Boolean
}

class LibraryObjectFields {
    companion object {
        const val ID: String = "Id"
        const val NAME: String = "Name"
        const val REMOVED: String = "Removed"
    }
}