package pl.yalgrin.playnite.simplesync.library.dto

import java.io.Serializable

//TODO: migrate to new format: filters presets, platform diffs, games, game diffs

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