package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.library.domain.Tag
import pl.yalgrin.playnite.simplesync.library.dto.TagDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelTagDTO

class PanelTagAssertionUtil {
    static boolean assertTag(PanelTagDTO expectedDTO, PanelTagDTO resultDTO) {
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

    static boolean assertTag(PanelTagDTO expectedDTO, TagDTO resultDTO) {
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

    static boolean assertTagEntity(PanelTagDTO expectedDTO, Tag resultDTO) {
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
