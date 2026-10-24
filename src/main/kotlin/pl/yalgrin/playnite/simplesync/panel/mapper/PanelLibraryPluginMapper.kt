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
        result.iconUrl =
            if (entity.iconMd5 != null) "/api/panel/library-plugin/${entity.id}/metadata/Icon/${entity.iconMd5}" else null
        result.backgroundImageUrl =
            if (entity.backgroundImageMd5 != null) "/api/panel/library-plugin/${entity.id}/metadata/BackgroundImage/${entity.backgroundImageMd5}" else null
        return result
    }

    override fun createDTO(): PanelLibraryPluginDTO = PanelLibraryPluginDTO()

    override fun createDiffDTO(): LibraryPluginDiffDTO = LibraryPluginDiffDTO()
}