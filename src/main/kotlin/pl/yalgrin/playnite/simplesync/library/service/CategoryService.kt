package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Category
import pl.yalgrin.playnite.simplesync.library.dto.CategoryDTO
import pl.yalgrin.playnite.simplesync.library.mapper.CategoryMapper
import pl.yalgrin.playnite.simplesync.library.repository.CategoryRepository
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCategoryDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelCategoryMapper
import reactor.core.publisher.Mono
import java.util.*

@Service
class CategoryService(
    repository: CategoryRepository,
    mapper: CategoryMapper,
    panelMapper: PanelCategoryMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<CategoryDTO, Category, PanelCategoryDTO>(
    repository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: CategoryDTO): Category {
        return Category(
            playniteId = dto.id
        )
    }

    override fun createEntityFromPanelDTO(dto: PanelCategoryDTO): Category {
        return Category(
            playniteId = UUID.randomUUID().toString()
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.CATEGORY
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<Category>): Mono<LibrarySaveResult<Category>> {
        return gameService.updateCategories(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Category>>): Mono<List<ChangeDTO>> {
        return gameService.updateCategoriesAfterDelete(results)
    }
}
