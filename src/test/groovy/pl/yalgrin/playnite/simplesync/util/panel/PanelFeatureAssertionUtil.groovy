package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.library.domain.Feature
import pl.yalgrin.playnite.simplesync.library.dto.FeatureDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelFeatureDTO

class PanelFeatureAssertionUtil {
    static boolean assertFeature(PanelFeatureDTO expectedDTO, PanelFeatureDTO resultDTO) {
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

    static boolean assertFeature(PanelFeatureDTO expectedDTO, FeatureDTO resultDTO) {
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
        true
    }

    static boolean assertFeatureEntity(PanelFeatureDTO expectedDTO, Feature resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getPlayniteId() != null
        assert resultDTO.getName() == expectedDTO.getName()
        assert !resultDTO.isRemoved()
        true
    }
}
