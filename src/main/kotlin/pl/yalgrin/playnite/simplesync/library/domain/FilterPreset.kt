package pl.yalgrin.playnite.simplesync.library.domain

import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import pl.yalgrin.playnite.simplesync.common.util.asObject
import pl.yalgrin.playnite.simplesync.library.dto.FilterPresetDatabaseModel
import reactor.core.publisher.Mono

object FilterPresetConstants {
    const val CURRENT_MODEL_VERSION = 2L
}

@Table("playnite_filter_preset")
data class FilterPreset(
    @Id
    @Column("id")
    override var id: Long? = null,

    @Column("playnite_id")
    override var playniteId: String? = null,

    @Column("name")
    override var name: String? = null,

    @Column("contents")
    var savedData: Json? = null,

    @Column("model_version")
    var modelVersion: Long = FilterPresetConstants.CURRENT_MODEL_VERSION,

    @Column("removed")
    override var isRemoved: Boolean = false,

    @Transient
    override var isNotifyAll: Boolean = false,

    @Transient
    override var isChanged: Boolean = false
) : LibraryObjectEntity

fun FilterPreset.extractDbModelOrEmpty(): Mono<FilterPresetDatabaseModel> {
    return this.savedData.asObject(FilterPresetDatabaseModel::class.java)
        .switchIfEmpty(Mono.fromSupplier { FilterPresetDatabaseModel() })
}