package pl.yalgrin.playnite.simplesync.repository.objects;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.objects.Game;
import reactor.core.publisher.Flux;

@Repository
public interface GameRepository extends ObjectRepository<Game> {
    @Query("select c.id from playnite_game c order by c.id asc")
    Flux<Long> findAllIds();

    @Query("select g.* from playnite_game g " +
            "where g.game_id = :gameId and g.plugin_id = :pluginId" +
            " order by g.id")
    Flux<Game> findByGameIdAndPluginId(String gameId, String pluginId);

    @Query("select g.*" +
            " from playnite_game g" +
            " where g.playnite_id = :playniteId" +
            " and g.game_id = :gameId" +
            " and g.plugin_id = :pluginId" +
            " and g.removed = false" +
            " order by g.id")
    Flux<Game> findByPlayniteIdAndGameIdAndPluginIdAndRemovedIsFalse(String playniteId, String gameId, String pluginId);

    @Query("select id from playnite_game where id in (:ids)")
    Flux<Long> findIdsByIds(Iterable<Long> ids);
}
