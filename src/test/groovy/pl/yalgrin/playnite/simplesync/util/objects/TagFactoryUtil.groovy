package pl.yalgrin.playnite.simplesync.util.objects

import pl.yalgrin.playnite.simplesync.dto.objects.TagDTO

class TagFactoryUtil {
    static TagDTO createTag(String id, String name, boolean removed = false) {
        return TagDTO.builder()
                .id(id)
                .name(name)
                .removed(removed)
                .build()
    }

    static TagDTO randomTag() {
        return TagDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .build()
    }

    static TagDTO tagWithIndex(int idx) {
        return TagDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .build()
    }
}
