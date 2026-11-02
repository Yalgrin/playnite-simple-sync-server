package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.Platform
import reactor.core.publisher.Flux

@Repository
interface PlatformRepository : ObjectRepository<Platform> {
    @Query("select c.id from playnite_platform c order by c.id")
    override fun findAllIds(): Flux<Long>

    @Query("select id from playnite_platform where id in (:ids)")
    fun findIdsByIds(ids: Iterable<Long>): Flux<Long>

    @Query("select distinct p.specification_id from playnite_platform p where p.specification_id is not null and p.removed = false")
    fun findSpecificationIds(): Flux<String>
}