package pl.yalgrin.playnite.simplesync.library.dto

import java.io.Serializable

//TODO: separate DB models and frontend DTOs
//TODO: migrate to new format: filters presets, platform diffs, games, game diffs
//TODO: finish moving remaining services to Kotlin

interface LibraryObjectDTO : Serializable {
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