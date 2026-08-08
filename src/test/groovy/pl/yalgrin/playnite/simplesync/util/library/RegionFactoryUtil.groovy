package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.RegionDTO

class RegionFactoryUtil {
    static RegionDTO createRegion(String id, String name, boolean removed = false) {
        return new RegionDTO(id, name, removed, UUID.randomUUID().toString())
    }

    static RegionDTO randomRegion() {
        return createRegion(UUID.randomUUID().toString(), UUID.randomUUID().toString())

    }

    static RegionDTO regionWithIndex(int idx) {
        return createRegion("id-$idx", "name-$idx")
    }
}
