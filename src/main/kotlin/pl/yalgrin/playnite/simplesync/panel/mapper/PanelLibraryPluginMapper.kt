package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.LibraryPlugin
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDiffDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelLibraryPluginDTO

@Component
class PanelLibraryPluginMapper :
    PanelObjectWithDiffMapperImpl<LibraryPlugin, PanelLibraryPluginDTO, LibraryPluginDiffDTO>() {

    override fun fillBasicFields(
        dto: PanelLibraryPluginDTO,
        entity: LibraryPlugin,
        generatedDiffDTO: LibraryPluginDiffDTO
    ): Triple<LibraryPlugin, LibraryPluginDiffDTO, MutableList<String>> {
        val result = super.fillBasicFields(dto, entity, generatedDiffDTO)
        val changedEntity = result.first

        changedEntity.isSavingIcon = dto.isSavingIcon
        changedEntity.isSavingBackground = dto.isSavingBackground
        return result
    }

    override fun fillBasicDtoFields(dto: PanelLibraryPluginDTO, entity: LibraryPlugin): PanelLibraryPluginDTO {
        val result = super.fillBasicDtoFields(dto, entity)
        result.hasIcon = entity.iconMd5 != null
        result.hasBackgroundImage = entity.backgroundImageMd5 != null
        result.iconHash = entity.iconMd5
        result.backgroundImageHash = entity.backgroundImageMd5
        return result
    }

    override fun createDTO(): PanelLibraryPluginDTO = PanelLibraryPluginDTO()

    override fun createDiffDTO(): LibraryPluginDiffDTO = LibraryPluginDiffDTO()
}