package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Region
import pl.yalgrin.playnite.simplesync.library.dto.RegionDTO
import pl.yalgrin.playnite.simplesync.library.mapper.RegionMapper
import pl.yalgrin.playnite.simplesync.library.repository.RegionRepository

@Service
class RegionService(
    repository: RegionRepository,
    mapper: RegionMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager
) : BaseLibraryObjectService<RegionDTO, Region>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: RegionDTO): Region {
        return Region(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.REGION
    }
}