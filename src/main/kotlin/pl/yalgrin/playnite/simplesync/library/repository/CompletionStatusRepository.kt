package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.CompletionStatus
import reactor.core.publisher.Flux

@Repository
interface CompletionStatusRepository : ObjectRepository<CompletionStatus> {
    @Query("select c.id from playnite_comp_status c order by c.id")
    override fun findAllIds(): Flux<Long>
}
