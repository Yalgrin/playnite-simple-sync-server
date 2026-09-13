package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.domain.LibraryPlugin
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDTO

class LibraryPluginAssertionUtil {
    static boolean assertLibraryPlugin(LibraryPluginDTO expectedDTO, LibraryPluginDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }

    static boolean assertLibraryPluginEntity(LibraryPluginDTO expectedDTO, LibraryPlugin resultEntity) {
        if (expectedDTO == null) {
            assert resultEntity == null
            return true
        }
        assert resultEntity != null
        assert resultEntity.getPlayniteId() == expectedDTO.getId()
        assert resultEntity.getName() == expectedDTO.getName()
        assert resultEntity.isRemoved() == expectedDTO.isRemoved()
        true
    }
}
