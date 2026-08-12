package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Source
import pl.yalgrin.playnite.simplesync.library.dto.SourceDTO
import pl.yalgrin.playnite.simplesync.library.mapper.SourceMapper
import pl.yalgrin.playnite.simplesync.library.repository.SourceRepository

@Service
class SourceService(
    repository: SourceRepository,
    mapper: SourceMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager
) : BaseLibraryObjectService<SourceDTO, Source>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: SourceDTO): Source {
        return Source(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.SOURCE
    }
}