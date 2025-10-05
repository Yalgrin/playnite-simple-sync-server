package pl.yalgrin.playnite.simplesync.util.objects

import pl.yalgrin.playnite.simplesync.dto.objects.CategoryDTO

class CategoryFactoryUtil {
    static CategoryDTO createCategory(String id, String name, boolean removed = false) {
        return CategoryDTO.builder()
                .id(id)
                .name(name)
                .removed(removed)
                .build()
    }

    static CategoryDTO randomCategory() {
        return CategoryDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .build()
    }

    static CategoryDTO categoryWithIndex(int idx) {
        return CategoryDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .build()
    }
}
