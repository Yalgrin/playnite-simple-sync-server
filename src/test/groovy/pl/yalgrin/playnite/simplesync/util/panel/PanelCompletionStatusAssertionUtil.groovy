package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.library.domain.CompletionStatus
import pl.yalgrin.playnite.simplesync.library.dto.CompletionStatusDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCompletionStatusDTO

class PanelCompletionStatusAssertionUtil {
    static boolean assertCompletionStatus(PanelCompletionStatusDTO expectedDTO, PanelCompletionStatusDTO resultDTO) {
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

    static boolean assertCompletionStatus(PanelCompletionStatusDTO expectedDTO, CompletionStatusDTO resultDTO) {
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

    static boolean assertCompletionStatusEntity(PanelCompletionStatusDTO expectedDTO, CompletionStatus resultDTO) {
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
