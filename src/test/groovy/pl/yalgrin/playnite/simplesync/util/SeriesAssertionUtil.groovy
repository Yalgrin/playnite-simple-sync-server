package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.domain.Series
import pl.yalgrin.playnite.simplesync.dto.SeriesDTO

class SeriesAssertionUtil {
    static boolean assertSeries(SeriesDTO expectedDTO, SeriesDTO resultDTO) {
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

    static boolean assertSeriesEntity(SeriesDTO expectedDTO, Series resultEntity) {
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
