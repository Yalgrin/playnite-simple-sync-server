package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.LibraryPlugin
import reactor.core.publisher.Flux

@Repository
interface LibraryPluginRepository : ObjectRepository<LibraryPlugin> {
    @Query("select c.id from playnite_plugin c order by c.id")
    override fun findAllIds(): Flux<Long>

    @Query("select id from playnite_plugin where id in (:ids)")
    fun findIdsByIds(ids: Iterable<Long>): Flux<Long>
}