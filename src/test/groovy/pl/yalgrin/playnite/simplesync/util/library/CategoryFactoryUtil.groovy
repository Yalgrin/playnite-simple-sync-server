package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.CategoryDTO

class CategoryFactoryUtil {
    static CategoryDTO createCategory(String id, String name, boolean removed = false) {
        return new CategoryDTO(id, name, removed)
    }

    static CategoryDTO randomCategory() {
        return createCategory(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static CategoryDTO categoryWithIndex(int idx) {
        return createCategory("id-$idx", "name-$idx")
    }
}
