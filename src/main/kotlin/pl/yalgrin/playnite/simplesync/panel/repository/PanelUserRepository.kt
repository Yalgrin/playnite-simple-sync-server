package pl.yalgrin.playnite.simplesync.panel.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.panel.domain.PanelUser
import reactor.core.publisher.Mono

@Repository
interface PanelUserRepository : R2dbcRepository<PanelUser, Long> {
    @Query("select u.* from panel_user u where u.username = :username")
    fun findByUsername(username: String): Mono<PanelUser>

    @Query("select exists(select 1 from panel_user)")
    fun existsAny(): Mono<Boolean>
}