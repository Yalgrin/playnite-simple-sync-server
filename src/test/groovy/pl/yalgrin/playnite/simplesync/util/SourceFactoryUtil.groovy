package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.dto.SourceDTO

class SourceFactoryUtil {
    static SourceDTO createSource(String id, String name, boolean removed = false) {
        return SourceDTO.builder()
                .id(id)
                .name(name)
                .removed(removed)
                .build()
    }

    static SourceDTO randomSource() {
        return SourceDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .build()
    }

    static SourceDTO sourceWithIndex(int idx) {
        return SourceDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .build()
    }
}
