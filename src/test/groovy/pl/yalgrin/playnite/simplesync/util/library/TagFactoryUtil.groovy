package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.TagDTO

class TagFactoryUtil {
    static TagDTO createTag(String id, String name, boolean removed = false) {
        return new TagDTO(id, name, removed)
    }

    static TagDTO randomTag() {
        return createTag(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static TagDTO tagWithIndex(int idx) {
        return createTag("id-$idx", "name-$idx")
    }
}
