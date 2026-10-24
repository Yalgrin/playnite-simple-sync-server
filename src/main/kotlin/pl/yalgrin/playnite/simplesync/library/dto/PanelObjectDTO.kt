package pl.yalgrin.playnite.simplesync.library.dto

import java.io.Serializable

interface PanelObjectDTO : Serializable {
    var id: Long?
    var playniteId: String?
    var name: String?
}