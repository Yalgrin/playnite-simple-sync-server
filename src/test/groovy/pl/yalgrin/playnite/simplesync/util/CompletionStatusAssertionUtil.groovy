package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.domain.CompletionStatus
import pl.yalgrin.playnite.simplesync.dto.CompletionStatusDTO

class CompletionStatusAssertionUtil {
    static boolean assertCompletionStatus(CompletionStatusDTO expectedDTO, CompletionStatusDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }

    static boolean assertCompletionStatusEntity(CompletionStatusDTO expectedDTO, CompletionStatus resultEntity) {
        if (expectedDTO == null) {
            assert resultEntity == null
            return true
        }
        assert resultEntity != null
        assert resultEntity.getPlayniteId() == expectedDTO.getId()
        assert resultEntity.getName() == expectedDTO.getName()
        assert resultEntity.isRemoved() == expectedDTO.isRemoved()
        true
    }
}
