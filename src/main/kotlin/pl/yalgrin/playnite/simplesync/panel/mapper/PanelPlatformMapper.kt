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
        if (!Strings.CS.equals(entity.specificationId, dto.specificationId)) {
            result.first.specificationId = dto.specificationId
            result.second.specificationId = dto.specificationId
            result.third.add("SpecificationId")
        }
        return result
    }

    override fun fillBasicDtoFields(dto: PanelPlatformDTO, entity: Platform): PanelPlatformDTO {
        val result = super.fillBasicDtoFields(dto, entity)
        result.specificationId = entity.specificationId
        result.hasIcon = entity.iconMd5 != null
        result.hasCoverImage = entity.coverImageMd5 != null
        result.hasBackgroundImage = entity.backgroundImageMd5 != null
        return result
    }

    override fun createDTO(): PanelPlatformDTO = PanelPlatformDTO()

    override fun createDiffDTO(): PlatformDiffDTO = PlatformDiffDTO()
}