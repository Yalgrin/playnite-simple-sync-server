package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.library.domain.Platform
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelPlatformDTO

class PanelPlatformAssertionUtil {
    static boolean assertPlatform(PanelPlatformDTO expectedDTO, PanelPlatformDTO resultDTO) {
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
        assert resultDTO.getSpecificationId() == expectedDTO.getSpecificationId()
        true
    }

    static boolean assertPlatform(PanelPlatformDTO expectedDTO, PlatformDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getServerId() != null
        assert resultDTO.getId() != null
        if (expectedDTO.getId() != null) {
            assert resultDTO.getServerId() == expectedDTO.getId()
        }
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.getSpecificationId() == expectedDTO.getSpecificationId()
        true
    }

    static boolean assertPlatformEntity(PanelPlatformDTO expectedDTO, Platform resultEntity) {
        if (expectedDTO == null) {
            assert resultEntity == null
            return true
        }
        assert resultEntity != null
        assert resultEntity.getPlayniteId() == expectedDTO.getId()
        assert resultEntity.getName() == expectedDTO.getName()
        assert resultEntity.getSpecificationId() == expectedDTO.getSpecificationId()
        assert !resultEntity.isRemoved()
        true
    }
}
