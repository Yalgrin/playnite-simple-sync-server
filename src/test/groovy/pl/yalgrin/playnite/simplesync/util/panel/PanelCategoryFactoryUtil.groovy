package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.panel.dto.PanelCategoryDTO

class PanelCategoryFactoryUtil {
    static PanelCategoryDTO createCategory(Long id, String name) {
        return new PanelCategoryDTO(id, name)
    }

    static PanelCategoryDTO createCategory(String name) {
        return new PanelCategoryDTO(null, name)
    }

    static PanelCategoryDTO randomCategory() {
        return createCategory(UUID.randomUUID().toString())
    }

    static PanelCategoryDTO categoryWithIndex(int idx) {
        return createCategory("name-$idx")
    }
}
