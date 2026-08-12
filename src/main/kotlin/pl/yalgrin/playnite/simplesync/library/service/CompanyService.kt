package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Company
import pl.yalgrin.playnite.simplesync.library.dto.CompanyDTO
import pl.yalgrin.playnite.simplesync.library.mapper.CompanyMapper
import pl.yalgrin.playnite.simplesync.library.repository.CompanyRepository

@Service
class CompanyService(
    repository: CompanyRepository,
    mapper: CompanyMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager
) : BaseLibraryObjectService<CompanyDTO, Company>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: CompanyDTO): Company {
        return Company(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.COMPANY
    }
}