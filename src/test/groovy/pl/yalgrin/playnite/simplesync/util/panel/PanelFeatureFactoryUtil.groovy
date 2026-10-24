package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.panel.dto.PanelFeatureDTO

class PanelFeatureFactoryUtil {
    static PanelFeatureDTO createFeature(Long id, String name) {
        return new PanelFeatureDTO(id, null, name)
    }

    static PanelFeatureDTO createFeature(String name) {
        return new PanelFeatureDTO(null, null, name)
    }

    static PanelFeatureDTO randomFeature() {
        return createFeature(UUID.randomUUID().toString())
    }

    static PanelFeatureDTO featureWithIndex(int idx) {
        return createFeature("name-$idx")
    }
}
