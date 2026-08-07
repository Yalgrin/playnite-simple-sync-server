package pl.yalgrin.playnite.simplesync.library.domain

interface LibraryObjectEntity {
    var id: Long?

    var playniteId: String?

    var name: String?

    var isRemoved: Boolean

    var isNotifyAll: Boolean

    var isChanged: Boolean
}