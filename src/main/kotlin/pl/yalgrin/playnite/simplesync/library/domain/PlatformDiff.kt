package pl.yalgrin.playnite.simplesync.library.domain

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import pl.yalgrin.playnite.simplesync.common.util.asObject
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDatabaseModel
import reactor.core.publisher.Mono
import java.time.Instant

object PlatformDiffConstants {
    const val CURRENT_MODEL_VERSION = 2L
}

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

    @Column("model_version")
    var modelVersion: Long = PlatformDiffConstants.CURRENT_MODEL_VERSION,

    @Column("removed")
    override var isRemoved: Boolean = false,

    @Column("created_at")
    override var createdAt: Instant = Instant.now(),

    @Column("created_by")
    override var createdBy: String? = null,

    @Transient
    var isNotifyAll: Boolean = false,

    @Transient
    var isChanged: Boolean = false,

    @Transient
    override var isForEntireObject: Boolean = false
) : LibraryObjectDiffEntity

fun PlatformDiff.extractDbModelOrEmpty(): Mono<PlatformDiffDatabaseModel> {
    return this.diffData.asObject(PlatformDiffDatabaseModel::class.java)
        .switchIfEmpty(Mono.fromSupplier { PlatformDiffDatabaseModel() })
}