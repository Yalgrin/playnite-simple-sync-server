package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.panel.dto.PanelRegionDTO

class PanelRegionFactoryUtil {
    static PanelRegionDTO createRegion(Long id, String name, String specificationId) {
        return new PanelRegionDTO(id, null, name, specificationId)
    }

    static PanelRegionDTO createRegion(String name, String specificationId) {
        return new PanelRegionDTO(null, null, name, specificationId)
    }

    static PanelRegionDTO createRegion(String name) {
        return new PanelRegionDTO(null, null, name, UUID.randomUUID().toString())
    }

    static PanelRegionDTO randomRegion() {
        return createRegion(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static PanelRegionDTO regionWithIndex(int idx) {
        return createRegion("name-$idx", "spec-$idx")
    }
}
