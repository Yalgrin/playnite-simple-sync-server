package pl.yalgrin.playnite.simplesync.library.dto

import java.io.Serializable
import java.time.Instant

interface LibraryObjectDTO : Serializable {
    var serverId: Long?
    var id: String?
    var name: String?
    var isRemoved: Boolean
    var createdAt: Instant?
    var createdBy: String?
    var modifiedAt: Instant?
    var modifiedBy: String?
}

class LibraryObjectFields {
    companion object {
        const val ID: String = "Id"
        const val NAME: String = "Name"
        const val REMOVED: String = "Removed"
    }
}