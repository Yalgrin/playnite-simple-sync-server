package pl.yalgrin.playnite.simplesync.panel.mapper

import org.apache.commons.lang3.Strings
import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.Platform
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelPlatformDTO

@Component
class PanelPlatformMapper :
    PanelObjectWithDiffMapperImpl<Platform, PanelPlatformDTO, PlatformDiffDTO>() {

    override fun fillBasicFields(
        dto: PanelPlatformDTO,
        entity: Platform,
        generatedDiffDTO: PlatformDiffDTO
    ): Triple<Platform, PlatformDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(dto, entity, generatedDiffDTO)
        val changedEntity = result.first
        val changedDiff = result.second
        val changedFields = result.third

        if (!Strings.CS.equals(entity.specificationId, dto.specificationId)) {
            changedEntity.specificationId = dto.specificationId
            changedDiff.specificationId = dto.specificationId
            changedFields.add("SpecificationId")
        }

        changedEntity.isSavingIcon = dto.isSavingIcon
        changedEntity.isSavingCoverImage = dto.isSavingCoverImage
        changedEntity.isSavingBackground = dto.isSavingBackground
        return result
    }

    override fun fillBasicDtoFields(dto: PanelPlatformDTO, entity: Platform): PanelPlatformDTO {
        val result = super.fillBasicDtoFields(dto, entity)
        result.specificationId = entity.specificationId
        result.hasIcon = entity.iconMd5 != null
        result.hasCoverImage = entity.coverImageMd5 != null
        result.hasBackgroundImage = entity.backgroundImageMd5 != null
        result.iconHash = entity.iconMd5
        result.coverImageHash = entity.coverImageMd5
        result.backgroundImageHash = entity.backgroundImageMd5
        return result
    }

    override fun createDTO(): PanelPlatformDTO = PanelPlatformDTO()

    override fun createDiffDTO(): PlatformDiffDTO = PlatformDiffDTO()
}