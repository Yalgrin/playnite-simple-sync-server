package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Company
import pl.yalgrin.playnite.simplesync.library.dto.CompanyDTO
import pl.yalgrin.playnite.simplesync.library.mapper.CompanyMapper
import pl.yalgrin.playnite.simplesync.library.repository.CompanyRepository
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCompanyDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelCompanyMapper
import reactor.core.publisher.Mono
import java.util.*

@Service
class CompanyService(
    repository: CompanyRepository,
    mapper: CompanyMapper,
    panelMapper: PanelCompanyMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager,
    @param:Lazy
    private val gameService: GameService
) : BaseLibraryObjectService<CompanyDTO, Company, PanelCompanyDTO>(
    repository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: CompanyDTO): Company {
        return Company(
            playniteId = dto.id
        )
    }

    override fun createEntityFromPanelDTO(dto: PanelCompanyDTO): Company {
        return Company(
            playniteId = UUID.randomUUID().toString()
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.COMPANY
    }

    override fun doActionsAfterSave(result: LibrarySaveResult<Company>): Mono<LibrarySaveResult<Company>> {
        return gameService.updateCompanies(result)
    }

    override fun doActionsAfterDelete(results: List<LibrarySaveResult<Company>>): Mono<List<ChangeDTO>> {
        return gameService.updateCompaniesAfterDelete(results)
    }
}