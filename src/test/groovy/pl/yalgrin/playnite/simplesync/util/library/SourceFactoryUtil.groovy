package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.SourceDTO

class SourceFactoryUtil {
    static SourceDTO createSource(String id, String name, boolean removed = false) {
        return new SourceDTO(id, name, removed)
    }

    static SourceDTO randomSource() {
        return createSource(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static SourceDTO sourceWithIndex(int idx) {
        return createSource("id-$idx", "name-$idx")
    }
}
