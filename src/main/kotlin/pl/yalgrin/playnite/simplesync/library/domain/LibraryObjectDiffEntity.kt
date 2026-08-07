package pl.yalgrin.playnite.simplesync.library.domain

import io.r2dbc.postgresql.codec.Json

interface LibraryObjectDiffEntity {
    var id: Long?

    var playniteId: String?

    var name: String?

    var diffData: Json?

    var isRemoved: Boolean
}