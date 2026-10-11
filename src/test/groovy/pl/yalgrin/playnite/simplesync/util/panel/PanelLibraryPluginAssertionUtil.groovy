package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.library.domain.LibraryPlugin
import pl.yalgrin.playnite.simplesync.panel.dto.PanelLibraryPluginDTO

class PanelLibraryPluginAssertionUtil {
    static boolean assertLibraryPlugin(PanelLibraryPluginDTO expectedDTO, PanelLibraryPluginDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getId() != null
        if (expectedDTO.getId() != null) {
            assert resultDTO.getId() == expectedDTO.getId()
        }
        assert resultDTO.getName() == expectedDTO.getName()
        true
    }

    static boolean assertLibraryPluginEntity(PanelLibraryPluginDTO expectedDTO, LibraryPlugin resultEntity) {
        if (expectedDTO == null) {
            assert resultEntity == null
            return true
        }
        assert resultEntity != null
        assert resultEntity.getPlayniteId() == expectedDTO.getId()
        assert resultEntity.getName() == expectedDTO.getName()
        assert !resultEntity.isRemoved()
        true
    }
}
