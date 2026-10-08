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
import pl.yalgrin.playnite.simplesync.panel.dto.PanelFilterPresetDTO
import pl.yalgrin.playnite.simplesync.panel.mapper.PanelFilterPresetMapper
import java.util.*

@Service
class FilterPresetService(
    repository: FilterPresetRepository,
    mapper: FilterPresetMapper,
    panelMapper: PanelFilterPresetMapper,
    changeService: ChangeService,
    changeListenerService: ChangeListenerService,
    transactionManager: ReactiveTransactionManager
) : BaseLibraryObjectService<FilterPresetDTO, FilterPreset, PanelFilterPresetDTO>(
    repository,
    mapper,
    panelMapper,
    changeService,
    changeListenerService,
    transactionManager
) {
    override fun createEntityFromDTO(dto: FilterPresetDTO): FilterPreset {
        return FilterPreset(
            playniteId = dto.id
        )
    }

    override fun createEntityFromPanelDTO(dto: PanelFilterPresetDTO): FilterPreset {
        return FilterPreset(
            playniteId = UUID.randomUUID().toString()
        )
    }

    override fun getObjectType(): ObjectType {
        return ObjectType.FILTER_PRESET
    }
}