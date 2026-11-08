package pl.yalgrin.playnite.simplesync.panel.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.common.helper.ITransactional
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.dto.GameDatabaseModel
import pl.yalgrin.playnite.simplesync.library.enums.GroupableField
import pl.yalgrin.playnite.simplesync.library.enums.toPastTimeSegment
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.library.service.CompletionStatusService
import pl.yalgrin.playnite.simplesync.library.service.LibraryPluginService
import pl.yalgrin.playnite.simplesync.panel.dto.*
import pl.yalgrin.playnite.simplesync.panel.repository.GameListCustomRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.text.Collator

@Service
class GameListService(
    private val gameListCustomRepository: GameListCustomRepository,
    private val gameRepository: GameRepository,
    private val libraryPluginService: LibraryPluginService,
    private val completionStatusService: CompletionStatusService,
    override val reactiveTransactionManager: ReactiveTransactionManager
) : ITransactional {
    fun findByFilter(filter: GameListFilterDTO): Mono<GameListResultDTO> {
        return gameListCustomRepository.findIdsByFilter(filter)
            .collectList()
            .flatMap { allIds ->
                Flux.fromIterable(allIds)
                    .buffer(100)
                    .flatMapSequential { ids ->
                        gameRepository.findAllById(ids)
                            .collectList()
                            .map { games ->
                                val groupedGames = games.groupBy { it.id }
                                ids.asSequence().flatMap { id -> groupedGames[id] ?: emptyList() }.toList()
                            }
                    }
                    .flatMapSequential { it.toFlux() }
                    .filter { it.id != null }
                    .flatMapSequential({ game ->
                        toGameList(game).flatMap { dto ->
                            game.extractDbModelOrEmpty().map { Triple(game, it, dto) }
                        }
                    })
                    .collectList()
            }
            .flatMap { fillRelatedFields(it) }
            .map { gameList ->
                val grouping = GroupableField.entries.find { it.code == filter.groupingOrder }
                if (grouping != null && grouping != GroupableField.NONE) {
                    val groupedResults = groupByField(gameList, grouping)
                    GameListResultDTO(
                        sortedGroups = groupedResults.first,
                        groupingOrder = grouping.code,
                        groupedResults = groupedResults.second
                    )
                } else {
                    GameListResultDTO(
                        ungroupedResults = gameList.map { it.third }
                    )
                }
            }
            .transactionalReadOnly()
    }

    private fun fillRelatedFields(gameList: List<Triple<Game, GameDatabaseModel, GameListDTO>>): Mono<List<Triple<Game, GameDatabaseModel, GameListDTO>>> {
        return findAllPlugins(gameList)
            .doOnNext { results ->
                gameList.forEach { triple ->
                    triple.third.plugin = triple.second.pluginId?.let { results[it]?.firstOrNull() }
                }
            }
            .then(
                findAllCompletionStatuses(gameList)
                    .doOnNext { results ->
                        gameList.forEach { triple ->
                            triple.third.completionStatus =
                                triple.second.completionStatus?.id?.let { results[it]?.firstOrNull() }
                        }
                    })
            .thenReturn(gameList)
    }

    private fun findAllPlugins(gameList: List<Triple<Game, GameDatabaseModel, GameListDTO>>): Mono<Map<String, List<PanelLibraryPluginDTO>>> {
        return Mono.fromSupplier {
            gameList.map { it.second }.mapNotNull { it.pluginId }.toSet()
        }.flatMap { ids ->
            ids.toFlux()
                .flatMap { id -> libraryPluginService.findByPlayniteIdForPanel(id).map { id to it } }
                .collectList()
        }.map { results ->
            results.groupBy({ it.first }, { it.second })
        }
    }

    private fun findAllCompletionStatuses(gameList: List<Triple<Game, GameDatabaseModel, GameListDTO>>): Mono<Map<String, List<PanelCompletionStatusDTO>>> {
        return Mono.fromSupplier {
            gameList.map { it.second }.mapNotNull { it.completionStatus }.mapNotNull { it.id }.toSet()
        }.flatMap { ids ->
            ids.toFlux()
                .flatMap { id -> completionStatusService.findByPlayniteIdForPanel(id).map { id to it } }
                .collectList()
        }.map { results ->
            results.groupBy({ it.first }, { it.second })
        }
    }

    private fun groupByField(
        list: List<Triple<Game, GameDatabaseModel, GameListDTO>>,
        grouping: GroupableField
    ): Pair<List<String>, Map<String, List<GameListDTO>>> {
        val map: MutableMap<String, MutableList<GameListDTO>> = mutableMapOf()
        for (game in list) {
            val selector = getSelector(game, grouping)
            if (selector.isEmpty()) {
                map.getOrPut("") { mutableListOf() }.add(game.third)
            } else {
                selector.forEach { key ->
                    map.getOrPut(key) { mutableListOf() }.add(game.third)
                }
            }
        }
        val comparator = getComparator(grouping)
        val sortedKeys = map.keys.sortedWith(comparator)
        return sortedKeys to map
    }

    private fun getComparator(grouping: GroupableField): Comparator<String> {
        var comparator: Comparator<String>
        if (grouping in setOf(
                GroupableField.LAST_ACTIVITY,
                GroupableField.PLAY_TIME,
                GroupableField.USER_SCORE,
                GroupableField.CRITIC_SCORE,
                GroupableField.COMMUNITY_SCORE,
                GroupableField.ADDED,
                GroupableField.MODIFIED,
                GroupableField.INSTALL_SIZE,
                GroupableField.LAST_ACTIVITY,
                GroupableField.RECENT_ACTIVITY,
            )
        ) {
            comparator = compareBy { it.toIntOrNull() ?: 999999 }
        } else if (grouping in setOf(
                GroupableField.RELEASE_YEAR
            )
        ) {
            comparator = compareBy { it.toIntOrNull() ?: -999999 }
        } else {
            comparator = Comparator<String> { s1, s2 ->
                if (s1.isEmpty() && s2.isNotEmpty()) {
                    return@Comparator 1
                } else if (s1.isNotEmpty() && s2.isEmpty()) {
                    return@Comparator -1
                }
                return@Comparator 0
            }.thenComparing(Collator.getInstance())
        }
        if (grouping in setOf(
                GroupableField.USER_SCORE,
                GroupableField.CRITIC_SCORE,
                GroupableField.COMMUNITY_SCORE,
                GroupableField.INSTALL_SIZE,
                GroupableField.RELEASE_YEAR,
                GroupableField.PLAY_TIME
            )
        ) {
            comparator = comparator.reversed()
        }
        return comparator
    }

    private fun getSelector(
        game: Triple<Game, GameDatabaseModel, GameListDTO>,
        grouping: GroupableField
    ): Set<String> {
        return when (grouping) {
            GroupableField.PLATFORM -> game.second.platforms.mapNotNull { it.name }.toSet()
            GroupableField.LIBRARY -> game.second.pluginName?.let { setOf(it) } ?: emptySet()
            GroupableField.CATEGORY -> game.second.categories.mapNotNull { it.name }.toSet()
            GroupableField.LAST_ACTIVITY -> game.second.lastActivity.toPastTimeSegment().value.toString()
                .let { setOf(it) }

            GroupableField.GENRE -> game.second.genres.mapNotNull { it.name }.toSet()
            GroupableField.RELEASE_YEAR -> game.second.releaseYear?.toString()?.let { setOf(it) } ?: emptySet()
            GroupableField.DEVELOPER -> game.second.developers.mapNotNull { it.name }.toSet()
            GroupableField.PUBLISHER -> game.second.publishers.mapNotNull { it.name }.toSet()
            GroupableField.TAG -> game.second.tags.mapNotNull { it.name }.toSet()
            GroupableField.SERIES -> game.second.series.mapNotNull { it.name }.toSet()
            GroupableField.AGE_RATING -> game.second.ageRatings.mapNotNull { it.name }.toSet()
            GroupableField.REGION -> game.second.regions.mapNotNull { it.name }.toSet()
            GroupableField.SOURCE -> game.second.source?.name?.let { setOf(it) } ?: emptySet()
            GroupableField.PLAY_TIME -> game.second.playtimeCategory.value.toString().let { setOf(it) }
            GroupableField.COMPLETION_STATUS -> game.second.completionStatus?.name?.let { setOf(it) } ?: emptySet()
            GroupableField.USER_SCORE -> game.second.userScoreGroup.value.toString().let { setOf(it) }
            GroupableField.CRITIC_SCORE -> game.second.criticScoreGroup.value.toString().let { setOf(it) }
            GroupableField.COMMUNITY_SCORE -> game.second.communityScoreGroup.value.toString().let { setOf(it) }
            GroupableField.ADDED -> game.second.added.toPastTimeSegment().value.toString().let { setOf(it) }
            GroupableField.MODIFIED -> game.second.modified.toPastTimeSegment().value.toString().let { setOf(it) }
            GroupableField.FEATURE -> game.second.features.mapNotNull { it.name }.toSet()
            GroupableField.INSTALLATION_STATUS -> setOf("false")
            GroupableField.NAME -> game.first.name?.let { getNameGroup(it) } ?: emptySet()
            GroupableField.INSTALL_DRIVE -> emptySet()
            GroupableField.INSTALL_SIZE -> game.second.installSizeGroup.value.toString().let { setOf(it) }
            GroupableField.RECENT_ACTIVITY -> game.second.recentActivity.toPastTimeSegment().value.toString()
                .let { setOf(it) }

            else -> {
                throw IllegalArgumentException("Unknown grouping field: $grouping")
            }
        }
    }

    private fun getNameGroup(name: String): Set<String> {
        if (name.isEmpty()) {
            return emptySet()
        }
        val firstChar = name[0].uppercaseChar()
        if (firstChar in 'A'..'Z') {
            return setOf(firstChar.toString())
        }
        return setOf("#")
    }

    private fun toGameList(game: Game): Mono<GameListDTO> {
        return game.extractDbModelOrEmpty()
            .map { databaseModel ->
                GameListDTO(
                    id = game.id!!,
                    name = game.name,
                    lastActivity = databaseModel.lastActivity,
                    playtime = databaseModel.playtime,
                    isFavorite = databaseModel.isFavorite,
                    hasIcon = game.iconMd5 != null,
                    iconUrl = if (game.iconMd5 != null) "/api/panel/game/${game.id}/metadata/Icon/${game.iconMd5}" else null,
                )
            }
    }

    fun findAllReleaseYears(): Mono<List<String>> {
        return gameRepository.findAllReleaseYears()
            .collectList()
            .transactionalReadOnly()
    }
}