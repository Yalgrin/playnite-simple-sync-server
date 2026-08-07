package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.Category
import reactor.core.publisher.Flux

@Repository
interface CategoryRepository : ObjectRepository<Category> {
    @Query("select c.id from playnite_category c order by c.id")
    override fun findAllIds(): Flux<Long>
}
