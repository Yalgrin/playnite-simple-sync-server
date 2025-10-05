package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.domain.Platform
import pl.yalgrin.playnite.simplesync.dto.PlatformDTO

class PlatformAssertionUtil {
    static boolean assertPlatform(PlatformDTO expectedDTO, PlatformDTO resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.getSpecificationId() == expectedDTO.getSpecificationId()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }

    static boolean assertPlatformEntity(PlatformDTO expectedDTO, Platform resultEntity) {
        if (expectedDTO == null) {
            assert resultEntity == null
            return true
        }
        assert resultEntity != null
        assert resultEntity.getPlayniteId() == expectedDTO.getId()
        assert resultEntity.getName() == expectedDTO.getName()
        assert resultEntity.getSpecificationId() == expectedDTO.getSpecificationId()
        assert resultEntity.isRemoved() == expectedDTO.isRemoved()
        true
    }
}
