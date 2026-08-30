package pl.yalgrin.playnite.simplesync.library.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("playnite_source")
data class Source(
    @Id
    @Column("id")
    override var id: Long? = null,

    @Column("playnite_id")
    override var playniteId: String? = null,

    @Column("name")
    override var name: String? = null,

    @Column("removed")
    override var isRemoved: Boolean = false,

    @Column("created_at")
    override var createdAt: Instant = Instant.now(),

    @Column("created_by")
    override var createdBy: String? = null,

    @Column("modified_at")
    override var modifiedAt: Instant = Instant.now(),

    @Column("modified_by")
    override var modifiedBy: String? = null,

    @Transient
    override var isNotifyAll: Boolean = false,

    @Transient
    override var isChanged: Boolean = false
) : LibraryObjectEntity