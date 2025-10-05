package pl.yalgrin.playnite.simplesync.util.objects

import pl.yalgrin.playnite.simplesync.domain.objects.Source
import pl.yalgrin.playnite.simplesync.dto.objects.SourceDTO

class SourceAssertionUtil {
    static boolean assertSource(SourceDTO expectedDTO, SourceDTO resultDTO) {
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

    static boolean assertSourceEntity(SourceDTO expectedDTO, Source resultEntity) {
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
