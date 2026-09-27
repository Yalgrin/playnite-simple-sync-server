package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Genre
import pl.yalgrin.playnite.simplesync.library.dto.GenreDTO
import pl.yalgrin.playnite.simplesync.library.mapper.GenreMapper
import pl.yalgrin.playnite.simplesync.library.repository.GenreRepository
import reactor.core.publisher.Mono

@Service
class GenreService(
    repository: GenreRepository,
    mapper: GenreMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<GenreDTO, Genre>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: GenreDTO): Genre {
        return Genre(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.GENRE
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<GenreDTO>): Mono<LibrarySaveResult<GenreDTO>> {
        return gameService.updateGenres(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Genre>>): Mono<List<ChangeDTO>> {
        return gameService.updateGenresAfterDelete(results)
    }
}