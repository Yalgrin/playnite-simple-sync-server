package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.AgeRating
import reactor.core.publisher.Flux

@Repository
interface AgeRatingRepository : ObjectRepository<AgeRating> {
    @Query("select c.id from playnite_age_rating c order by c.id")
    override fun findAllIds(): Flux<Long>
}
