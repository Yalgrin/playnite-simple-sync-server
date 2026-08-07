package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.Series
import reactor.core.publisher.Flux

@Repository
interface SeriesRepository : ObjectRepository<Series> {
    @Query("select c.id from playnite_series c order by c.id")
    override fun findAllIds(): Flux<Long>
}
