package pl.yalgrin.playnite.simplesync.library.domain

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("playnite_platform_diff")
data class PlatformDiff(
    @Id
    @Column("id")
    override var id: Long? = null,

    @Column("playnite_id")
    override var playniteId: String? = null,

    @Column("name")
    override var name: String? = null,

    @Column("contents")
    override var diffData: Json? = null,

    @Column("removed")
    override var isRemoved: Boolean = false,

    @Transient
    var isNotifyAll: Boolean = false,

    @Transient
    var isChanged: Boolean = false
) : LibraryObjectDiffEntity