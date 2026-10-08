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
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGenreDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelGenreMapper
import reactor.core.publisher.Mono
import java.util.*

@Service
class GenreService(
    repository: GenreRepository,
    mapper: GenreMapper,
    panelMapper: PanelGenreMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<GenreDTO, Genre, PanelGenreDTO>(
    repository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: GenreDTO): Genre {
        return Genre(
            playniteId = dto.id
        )
    }

    override fun createEntityFromPanelDTO(dto: PanelGenreDTO): Genre {
        return Genre(
            playniteId = UUID.randomUUID().toString()
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.GENRE
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<Genre>): Mono<LibrarySaveResult<Genre>> {
        return gameService.updateGenres(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Genre>>): Mono<List<ChangeDTO>> {
        return gameService.updateGenresAfterDelete(results)
    }
}