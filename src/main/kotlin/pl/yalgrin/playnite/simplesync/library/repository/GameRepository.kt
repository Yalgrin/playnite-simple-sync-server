package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.Game
import reactor.core.publisher.Flux

@Repository
interface GameRepository : ObjectRepository<Game> {
    @Query("select c.id from playnite_game c order by c.id")
    override fun findAllIds(): Flux<Long>

    @Query(
        "select g.* from playnite_game g " +
                "where g.game_id = :gameId and g.plugin_id = :pluginId" +
                " order by g.id"
    )
    fun findByGameIdAndPluginId(gameId: String, pluginId: String): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where g.playnite_id = :playniteId" +
                " and g.game_id = :gameId" +
                " and g.plugin_id = :pluginId" +
                " and g.removed = false" +
                " order by g.id"
    )
    fun findByPlayniteIdAndGameIdAndPluginIdAndRemovedIsFalse(
        playniteId: String,
        gameId: String,
        pluginId: String
    ): Flux<Game>

    @Query("select id from playnite_game where id in (:ids)")
    fun findIdsByIds(ids: Iterable<Long>): Flux<Long>
}