package pl.yalgrin.playnite.simplesync.library.mapper

import org.apache.commons.lang3.Strings
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.common.util.asJson
import pl.yalgrin.playnite.simplesync.library.domain.FilterPreset
import pl.yalgrin.playnite.simplesync.library.domain.extractDbModelOrEmpty
import pl.yalgrin.playnite.simplesync.library.dto.FilterPresetDTO
import pl.yalgrin.playnite.simplesync.library.dto.FilterPresetDatabaseModel
import pl.yalgrin.playnite.simplesync.library.dto.filter.FilterPresetSettingsDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.IdItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.IntItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.StringItemPropertiesDTO
import reactor.core.publisher.Mono

@Component
class FilterPresetMapper : LibraryObjectMapperImpl<FilterPreset, FilterPresetDTO>() {

    override fun fillOtherFields(entity: FilterPreset, dto: FilterPresetDTO): Mono<FilterPreset> {
        return entity.extractDbModelOrEmpty()
            .map { fillDbModelFields(it, dto) }
            .flatMap { dbModel -> dbModel.asJson() }
            .map { json -> entity.savedData = json }
            .thenReturn(entity)
    }

    private fun fillDbModelFields(dbModel: FilterPresetDatabaseModel, dto: FilterPresetDTO): FilterPresetDatabaseModel {
        dbModel.sortingOrder = dto.sortingOrder
        dbModel.sortingOrderDirection = dto.sortingOrderDirection
        dbModel.groupingOrder = dto.groupingOrder
        dbModel.showInFullscreenQuickSelection = dto.showInFullscreenQuickSelection
        dbModel.settings = dto.settings
        return dbModel
    }

    override fun hasChangedMono(dto: FilterPresetDTO, target: FilterPreset): Mono<Boolean> {
        return super.hasChangedMono(dto, target)
            .flatMap { changed -> if (changed) Mono.just(true) else isPreviousDataDifferent(target, dto) }
    }

    private fun isPreviousDataDifferent(target: FilterPreset, dto: FilterPresetDTO): Mono<Boolean> {
        return target.extractDbModelOrEmpty()
            .map { previousDTO ->
                !Strings.CS.equals(
                    previousDTO.sortingOrder,
                    dto.sortingOrder
                ) || !Strings.CS.equals(
                    previousDTO.sortingOrderDirection,
                    dto.sortingOrderDirection
                ) || !Strings.CS.equals(
                    previousDTO.groupingOrder,
                    dto.groupingOrder
                ) || (previousDTO.showInFullscreenQuickSelection != dto.showInFullscreenQuickSelection) || areSettingsDifferent(
                    previousDTO.settings,
                    dto.settings
                )
            }
            .defaultIfEmpty(true)
    }

    private fun areSettingsDifferent(previousDTO: FilterPresetSettingsDTO?, newDTO: FilterPresetSettingsDTO?): Boolean {
        return (previousDTO == null) != (newDTO == null)
                || (previousDTO != null && ((previousDTO.useAndFilteringStyle != newDTO!!.useAndFilteringStyle) || (previousDTO.isInstalled != newDTO.isInstalled) || (previousDTO.isUninstalled != newDTO.isUninstalled) || (previousDTO.isHidden != newDTO.isHidden) || (previousDTO.isFavorite != newDTO.isFavorite) || !Strings.CS.equals(
            previousDTO.name,
            newDTO.name
        ) || !Strings.CS.equals(
            previousDTO.version,
            newDTO.version
        ) || areItemPropertiesDifferent(previousDTO.releaseYear, newDTO.releaseYear)
                || areItemPropertiesDifferent(previousDTO.genre, newDTO.genre)
                || areItemPropertiesDifferent(previousDTO.platform, newDTO.platform)
                || areItemPropertiesDifferent(previousDTO.developer, newDTO.developer)
                || areItemPropertiesDifferent(previousDTO.category, newDTO.category)
                || areItemPropertiesDifferent(previousDTO.tag, newDTO.tag)
                || areItemPropertiesDifferent(previousDTO.series, newDTO.series)
                || areItemPropertiesDifferent(previousDTO.region, newDTO.region)
                || areItemPropertiesDifferent(previousDTO.source, newDTO.source)
                || areItemPropertiesDifferent(previousDTO.ageRating, newDTO.ageRating)
                || areItemPropertiesDifferent(previousDTO.library, newDTO.library)
                || areItemPropertiesDifferent(
            previousDTO.completionStatuses,
            newDTO.completionStatuses
        )
                || areItemPropertiesDifferent(previousDTO.feature, newDTO.feature)
                || areItemPropertiesDifferent(previousDTO.userScore, newDTO.userScore)
                || areItemPropertiesDifferent(previousDTO.criticScore, newDTO.criticScore)
                || areItemPropertiesDifferent(previousDTO.communityScore, newDTO.communityScore)
                || areItemPropertiesDifferent(previousDTO.lastActivity, newDTO.lastActivity)
                || areItemPropertiesDifferent(previousDTO.recentActivity, newDTO.recentActivity)
                || areItemPropertiesDifferent(previousDTO.added, newDTO.added)
                || areItemPropertiesDifferent(previousDTO.modified, newDTO.modified)
                || areItemPropertiesDifferent(previousDTO.playTime, newDTO.playTime)
                || areItemPropertiesDifferent(previousDTO.installSize, newDTO.installSize)
                ))
    }

    private fun areItemPropertiesDifferent(
        previousDTO: StringItemPropertiesDTO?,
        newDTO: StringItemPropertiesDTO?
    ): Boolean {
        return (previousDTO == null) != (newDTO == null)
                || (previousDTO != null && areListsDifferent(previousDTO.values, newDTO!!.values))
    }

    private fun areItemPropertiesDifferent(previousDTO: IdItemPropertiesDTO?, newDTO: IdItemPropertiesDTO?): Boolean {
        return (previousDTO == null) != (newDTO == null)
                || (previousDTO != null && (areListsDifferent(previousDTO.ids, newDTO!!.ids)
                || !Strings.CS.equals(previousDTO.text, newDTO.text)))
    }

    private fun areItemPropertiesDifferent(previousDTO: IntItemPropertiesDTO?, newDTO: IntItemPropertiesDTO?): Boolean {
        return (previousDTO == null) != (newDTO == null)
                || (previousDTO != null && areListsDifferent(previousDTO.values, newDTO!!.values))
    }

    private fun <T> areListsDifferent(previousList: List<T>, newList: List<T>): Boolean {
        val previousCount = previousList.size
        val newCount = newList.size
        if (previousCount != newCount) {
            return true
        }
        val newListCopy: MutableList<T> = ArrayList(newList)
        for (t in previousList) {
            if (!newListCopy.remove(t)) {
                return true
            }
        }
        return false
    }

    override fun createDTO(): FilterPresetDTO = FilterPresetDTO()

    override fun fillOtherDtoFields(dto: FilterPresetDTO, entity: FilterPreset): Mono<FilterPresetDTO> {
        return entity.extractDbModelOrEmpty()
            .map { dbModel ->
                dto.sortingOrder = dbModel.sortingOrder
                dto.sortingOrderDirection = dbModel.sortingOrderDirection
                dto.groupingOrder = dbModel.groupingOrder
                dto.showInFullscreenQuickSelection = dbModel.showInFullscreenQuickSelection
                dto.settings = dbModel.settings
                dto
            }
            .thenReturn(dto)
    }
}