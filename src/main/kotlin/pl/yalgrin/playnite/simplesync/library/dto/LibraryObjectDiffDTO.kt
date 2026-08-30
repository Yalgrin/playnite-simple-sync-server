package pl.yalgrin.playnite.simplesync.library.dto

import java.io.Serializable
import java.time.Instant

interface LibraryObjectDiffDTO : Serializable {
    var serverId: Long?
    var id: String?
    var name: String?
    var baseObjectId: Long?
    var changedFields: List<String>
    var isRemoved: Boolean
    var createdAt: Instant?
    var createdBy: String?
}