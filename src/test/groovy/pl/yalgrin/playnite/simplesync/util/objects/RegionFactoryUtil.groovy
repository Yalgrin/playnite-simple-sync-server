package pl.yalgrin.playnite.simplesync.util.objects

import pl.yalgrin.playnite.simplesync.dto.objects.RegionDTO

class RegionFactoryUtil {
    static RegionDTO createRegion(String id, String name, boolean removed = false) {
        return RegionDTO.builder()
                .id(id)
                .name(name)
                .specificationId(UUID.randomUUID().toString())
                .removed(removed)
                .build()
    }

    static RegionDTO randomRegion() {
        return RegionDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .specificationId(UUID.randomUUID().toString())
                .build()
    }

    static RegionDTO regionWithIndex(int idx) {
        return RegionDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .specificationId(UUID.randomUUID().toString())
                .build()
    }
}
