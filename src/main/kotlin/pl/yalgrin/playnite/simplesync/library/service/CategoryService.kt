package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Category
import pl.yalgrin.playnite.simplesync.library.dto.CategoryDTO
import pl.yalgrin.playnite.simplesync.library.mapper.CategoryMapper
import pl.yalgrin.playnite.simplesync.library.repository.CategoryRepository

@Service
class CategoryService(
    repository: CategoryRepository,
    mapper: CategoryMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager
) : BaseLibraryObjectService<CategoryDTO, Category>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: CategoryDTO): Category {
        return Category(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.CATEGORY
    }
}
