package pl.yalgrin.playnite.simplesync.library.service

import org.springframework.stereotype.Service
import org.springframework.transaction.ReactiveTransactionManager
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService
import pl.yalgrin.playnite.simplesync.change.service.ChangeService
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.FilterPreset
import pl.yalgrin.playnite.simplesync.library.dto.FilterPresetDTO
import pl.yalgrin.playnite.simplesync.library.mapper.FilterPresetMapper
import pl.yalgrin.playnite.simplesync.library.repository.FilterPresetRepository

@Service
class FilterPresetService(
    repository: FilterPresetRepository,
    mapper: FilterPresetMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager
) : BaseLibraryObjectService<FilterPresetDTO, FilterPreset>(
    repository,
    mapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: FilterPresetDTO): FilterPreset {
        return FilterPreset(
            playniteId = dto.id
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.FILTER_PRESET
    }
}