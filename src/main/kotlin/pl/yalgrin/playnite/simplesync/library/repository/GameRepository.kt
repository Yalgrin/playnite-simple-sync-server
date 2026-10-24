package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.Game
import reactor.core.publisher.Flux

@Repository
interface GameRepository : ObjectRepository<Game>, ModelVersionObjectRepository<Game> {
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

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where g.contents->>'pluginId' in (:ids)" +
                " order by g.id"
    )
    fun findByLibraryPluginIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'genres' AS jsonb)) genre" +
                " where genre->>'id' in (:ids)" +
                " )" +
                " order by g.id"
    )
    fun findByGenreIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'platforms' AS jsonb)) platform" +
                " where platform->>'id' in (:ids)" +
                " )" +
                " order by g.id"
    )
    fun findByPlatformIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'publishers' AS jsonb)) publisher" +
                " where publisher->>'id' in (:ids)" +
                " )" +
                " or exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'developers' AS jsonb)) developer" +
                " where developer->>'id' in (:ids)" +
                " )" +
                " order by g.id"
    )
    fun findByCompanyIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'categories' AS jsonb)) category" +
                " where category->>'id' in (:ids)" +
                " )" +
                " order by g.id"
    )
    fun findByCategoryIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'tags' AS jsonb)) tag" +
                " where tag->>'id' in (:ids)" +
                " )" +
                " order by g.id"
    )
    fun findByTagIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'features' AS jsonb)) feature" +
                " where feature->>'id' in (:ids)" +
                " )" +
                " order by g.id"
    )
    fun findByFeatureIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'series' AS jsonb)) series" +
                " where series->>'id' in (:ids)" +
                " )" +
                " order by g.id"
    )
    fun findBySeriesIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'ageRatings' AS jsonb)) age_rating" +
                " where age_rating->>'id' in (:ids)" +
                " )" +
                " order by g.id"
    )
    fun findByAgeRatingIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where exists (" +
                " select 1" +
                " from jsonb_array_elements(CAST(g.contents->'regions' AS jsonb)) region" +
                " where region->>'id' in (:ids)" +
                " )" +
                " order by g.id"
    )
    fun findByRegionIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where (CAST(g.contents->>'source' AS jsonb))->>'id' in (:ids)" +
                " order by g.id"
    )
    fun findBySourceIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select g.*" +
                " from playnite_game g" +
                " where (CAST(g.contents->>'completionStatus' AS jsonb))->>'id' in (:ids)" +
                " order by g.id"
    )
    fun findByCompletionStatusIds(ids: Collection<String>): Flux<Game>

    @Query(
        "select distinct g.contents->>'releaseYear'" +
                " from playnite_game g" +
                " where g.contents->>'releaseYear' is not null" +
                " order by 1"
    )
    fun findAllReleaseYears(): Flux<String>
}