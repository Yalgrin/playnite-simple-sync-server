package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.Feature
import reactor.core.publisher.Flux

@Repository
interface FeatureRepository : ObjectRepository<Feature> {
    @Query("select c.id from playnite_feature c order by c.id")
    override fun findAllIds(): Flux<Long>
}
