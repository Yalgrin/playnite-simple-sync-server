package pl.yalgrin.playnite.simplesync.util.objects

import pl.yalgrin.playnite.simplesync.domain.objects.Region
import pl.yalgrin.playnite.simplesync.dto.objects.RegionDTO

class RegionAssertionUtil {
    static boolean assertRegion(RegionDTO expectedDTO, RegionDTO resultDTO) {
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

    static boolean assertRegionEntity(RegionDTO expectedDTO, Region resultEntity) {
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
