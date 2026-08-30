package pl.yalgrin.playnite.simplesync.library.domain

import io.r2dbc.postgresql.codec.Json
import java.time.Instant

interface LibraryObjectDiffEntity {
    var id: Long?

    var playniteId: String?

    var name: String?

    var diffData: Json?

    var isRemoved: Boolean

    var isForEntireObject: Boolean

    var createdAt: Instant

    var createdBy: String?
}