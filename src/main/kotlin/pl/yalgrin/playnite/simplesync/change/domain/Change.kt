package pl.yalgrin.playnite.simplesync.change.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import java.time.Instant

@Table("playnite_change")
data class Change(
    @Id
    @Column("id")
    var id: Long? = null,

    @Column("type")
    var type: ObjectType,

    @Column("client_id")
    var clientId: String,

    @Column("object_id")
    var objectId: Long,

    @Column("created_at")
    var createdAt: Instant = Instant.now(),

    @Transient
    var notifyAll: Boolean = false
)
