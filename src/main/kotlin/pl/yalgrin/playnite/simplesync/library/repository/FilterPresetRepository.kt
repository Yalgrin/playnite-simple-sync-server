package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.FilterPreset
import reactor.core.publisher.Flux

@Repository
interface FilterPresetRepository : ObjectRepository<FilterPreset> {
    @Query("select c.id from playnite_filter_preset c order by c.id")
    override fun findAllIds(): Flux<Long>
}
