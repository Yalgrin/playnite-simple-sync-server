package pl.yalgrin.playnite.simplesync.panel.mapper

import org.springframework.stereotype.Component
import pl.yalgrin.playnite.simplesync.library.domain.LibraryPlugin
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDiffDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelLibraryPluginDTO

@Component
class PanelLibraryPluginMapper :
    PanelObjectWithDiffMapperImpl<LibraryPlugin, PanelLibraryPluginDTO, LibraryPluginDiffDTO>() {

    override fun createDTO(): PanelLibraryPluginDTO = PanelLibraryPluginDTO()

    override fun createDiffDTO(): LibraryPluginDiffDTO = LibraryPluginDiffDTO()
}