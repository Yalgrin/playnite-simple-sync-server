package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Tag
import pl.yalgrin.playnite.simplesync.library.dto.TagDTO
import pl.yalgrin.playnite.simplesync.library.mapper.TagMapper
import pl.yalgrin.playnite.simplesync.library.repository.TagRepository
import pl.yalgrin.playnite.simplesync.panel.dto.PanelTagDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelTagMapper
import reactor.core.publisher.Mono
import java.util.*

@Service
class TagService(
    repository: TagRepository,
    mapper: TagMapper,
    panelMapper: PanelTagMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<TagDTO, Tag, PanelTagDTO>(
    repository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: TagDTO): Tag {
        return Tag(
            playniteId = dto.id
        )
    }

    override fun createEntityFromPanelDTO(dto: PanelTagDTO): Tag {
        return Tag(
            playniteId = UUID.randomUUID().toString()
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.TAG
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<Tag>): Mono<LibrarySaveResult<Tag>> {
        return gameService.updateTags(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Tag>>): Mono<List<ChangeDTO>> {
        return gameService.updateTagsAfterDelete(results)
    }
}