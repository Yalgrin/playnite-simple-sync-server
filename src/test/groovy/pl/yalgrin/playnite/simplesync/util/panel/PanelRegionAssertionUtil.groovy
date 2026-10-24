package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.library.domain.Region
import pl.yalgrin.playnite.simplesync.library.dto.RegionDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelRegionDTO

class PanelRegionAssertionUtil {
    static boolean assertRegion(PanelRegionDTO expectedDTO, PanelRegionDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getId() != null
        if (expectedDTO.getId() != null) {
            assert resultDTO.getId() == expectedDTO.getId()
        }
        assert resultDTO.getPlayniteId() != null
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.getSpecificationId() == expectedDTO.getSpecificationId()
        true
    }

    static boolean assertRegion(PanelRegionDTO expectedDTO, RegionDTO resultDTO) {
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

    static boolean assertRegionEntity(PanelRegionDTO expectedDTO, Region resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getPlayniteId() != null
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.getSpecificationId() == expectedDTO.getSpecificationId()
        assert !resultDTO.isRemoved()
        true
    }
}
