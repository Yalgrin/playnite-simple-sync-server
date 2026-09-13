package pl.yalgrin.playnite.simplesync.library.domain

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import pl.yalgrin.playnite.simplesync.common.util.asObject
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDiffDatabaseModel
import reactor.core.publisher.Mono
import java.time.Instant

object LibraryPluginDiffConstants {
    const val CURRENT_MODEL_VERSION = 1L
}

@Table("playnite_plugin_diff")
data class LibraryPluginDiff(
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
    var modelVersion: Long = LibraryPluginDiffConstants.CURRENT_MODEL_VERSION,

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

fun LibraryPluginDiff.extractDbModelOrEmpty(): Mono<LibraryPluginDiffDatabaseModel> {
    return this.diffData.asObject(LibraryPluginDiffDatabaseModel::class.java)
        .switchIfEmpty(Mono.fromSupplier { LibraryPluginDiffDatabaseModel() })
}