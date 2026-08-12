package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.CompletionStatus
import pl.yalgrin.playnite.simplesync.library.dto.CompletionStatusDTO
import pl.yalgrin.playnite.simplesync.library.mapper.CompletionStatusMapper
import pl.yalgrin.playnite.simplesync.library.repository.CompletionStatusRepository

@Service
class CompletionStatusService(
    repository: CompletionStatusRepository,
    mapper: CompletionStatusMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager
) : BaseLibraryObjectService<CompletionStatusDTO, CompletionStatus>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: CompletionStatusDTO): CompletionStatus {
        return CompletionStatus(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.COMPLETION_STATUS
    }
}