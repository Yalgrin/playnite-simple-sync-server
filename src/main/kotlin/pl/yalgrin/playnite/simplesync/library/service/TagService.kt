package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Tag
import pl.yalgrin.playnite.simplesync.library.dto.TagDTO
import pl.yalgrin.playnite.simplesync.library.mapper.TagMapper
import pl.yalgrin.playnite.simplesync.library.repository.TagRepository

@Service
class TagService(
    repository: TagRepository,
    mapper: TagMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager
) : BaseLibraryObjectService<TagDTO, Tag>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: TagDTO): Tag {
        return Tag(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.TAG
    }
}