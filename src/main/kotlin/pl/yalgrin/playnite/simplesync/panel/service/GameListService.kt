package pl.yalgrin.playnite.simplesync.panel.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.common.util.transactionalReadOnly
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.panel.dto.GameListDTO
import pl.yalgrin.playnite.simplesync.panel.dto.GameListFilterDTO
import pl.yalgrin.playnite.simplesync.panel.dto.GameListResultDTO
import pl.yalgrin.playnite.simplesync.panel.repository.GameListCustomRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Service
class GameListService(
    private val gameListCustomRepository: GameListCustomRepository,
    private val gameRepository: GameRepository,
    private val reactiveTransactionManager: ReactiveTransactionManager
) {
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
                    .flatMapSequential { toGameList(it) }
                    .collectList()
            }
            .map { gameList ->
                GameListResultDTO(
                    ungroupedResults = gameList,
                    groupedResults = emptyMap()
                )
            }
            .transactionalReadOnly(reactiveTransactionManager)
    }

    private fun toGameList(game: Game): Mono<GameListDTO> {
        return game.extractDbModelOrEmpty()
            .map { databaseModel ->
                GameListDTO(
                    id = game.id!!,
                    name = game.name,
                    isFavorite = databaseModel.isFavorite,
                )
            }
    }
}