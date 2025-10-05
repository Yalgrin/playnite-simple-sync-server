package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.dto.FeatureDTO

class FeatureFactoryUtil {
    static FeatureDTO createFeature(String id, String name, boolean removed = false) {
        return FeatureDTO.builder()
                .id(id)
                .name(name)
                .removed(removed)
                .build()
    }

    static FeatureDTO randomFeature() {
        return FeatureDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .build()
    }

    static FeatureDTO featureWithIndex(int idx) {
        return FeatureDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .build()
    }
}
