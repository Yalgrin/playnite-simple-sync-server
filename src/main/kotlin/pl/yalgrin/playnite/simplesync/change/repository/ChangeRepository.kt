package pl.yalgrin.playnite.simplesync.change.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.change.domain.Change
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ChangeRepository : R2dbcRepository<Change, Long> {
    @Query("select c.* from playnite_change c where (:lastId is null or c.id > :lastId) and c.client_id <> :clientId order by c.id")
    fun findFromLastId(lastId: Long?, clientId: String): Flux<Change>

    @Query("select max(c.id) from playnite_change c")
    fun findMaxId(): Mono<Long>
}