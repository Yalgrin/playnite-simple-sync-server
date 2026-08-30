package pl.yalgrin.playnite.simplesync.library.domain

import java.time.Instant

interface LibraryObjectEntity {
    var id: Long?

    var playniteId: String?

    var name: String?

    var isRemoved: Boolean

    var isNotifyAll: Boolean

    var isChanged: Boolean

    var createdAt: Instant

    var createdBy: String?

    var modifiedAt: Instant

    var modifiedBy: String?
}