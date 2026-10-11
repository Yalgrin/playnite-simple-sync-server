package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.library.domain.Category
import pl.yalgrin.playnite.simplesync.library.dto.CategoryDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCategoryDTO

class PanelCategoryAssertionUtil {
    static boolean assertCategory(PanelCategoryDTO expectedDTO, PanelCategoryDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getId() != null
        if (expectedDTO.getId() != null) {
            assert resultDTO.getId() == expectedDTO.getId()
        }
        assert resultDTO.getName() == expectedDTO.getName()
        true
    }

    static boolean assertCategory(PanelCategoryDTO expectedDTO, CategoryDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getServerId() != null
        assert resultDTO.getId() != null
        if (expectedDTO.getId() != null) {
            assert resultDTO.getServerId() == expectedDTO.getId()
        }
        assert resultDTO.getName() == expectedDTO.getName()
        true
    }

    static boolean assertCategoryEntity(PanelCategoryDTO expectedDTO, Category resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getPlayniteId() != null
        assert resultDTO.getName() == expectedDTO.getName()
        assert !resultDTO.isRemoved()
        true
    }
}
