package pl.yalgrin.playnite.simplesync.library.domain

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import pl.yalgrin.playnite.simplesync.common.util.asObject
import pl.yalgrin.playnite.simplesync.library.dto.GameDiffDatabaseModel
import reactor.core.publisher.Mono

object GameDiffConstants {
    const val CURRENT_MODEL_VERSION = 2L
}

@Table("playnite_game_diff")
data class GameDiff(
    @Id
    @Column("id")
    override var id: Long? = null,

    @Column("playnite_id")
    override var playniteId: String? = null,

    @Column("game_id")
    var gameId: String? = null,

    @Column("plugin_id")
    var pluginId: String? = null,

    @Column("name")
    override var name: String? = null,

    @Column("contents")
    override var diffData: Json? = null,

    @Column("model_version")
    var modelVersion: Long = GameDiffConstants.CURRENT_MODEL_VERSION,

    @Column("removed")
    override var isRemoved: Boolean = false,

    @Transient
    var notifyAll: Boolean = false,

    @Transient
    var changed: Boolean = false,

    @Transient
    override var isForEntireObject: Boolean = false
) : LibraryObjectDiffEntity

fun GameDiff.extractDbModelOrEmpty(): Mono<GameDiffDatabaseModel> {
    return this.diffData.asObject(GameDiffDatabaseModel::class.java)
        .switchIfEmpty(Mono.fromSupplier { GameDiffDatabaseModel() })
}