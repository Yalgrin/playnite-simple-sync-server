package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.FeatureDTO

class FeatureFactoryUtil {
    static FeatureDTO createFeature(String id, String name, boolean removed = false) {
        return new FeatureDTO(id, name, removed)
    }

    static FeatureDTO randomFeature() {
        return createFeature(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static FeatureDTO featureWithIndex(int idx) {
        return createFeature("id-$idx", "name-$idx")
    }
}
