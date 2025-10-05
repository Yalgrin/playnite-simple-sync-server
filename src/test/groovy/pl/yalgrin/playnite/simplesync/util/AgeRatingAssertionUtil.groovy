package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.domain.AgeRating
import pl.yalgrin.playnite.simplesync.dto.AgeRatingDTO

class AgeRatingAssertionUtil {
    static boolean assertAgeRating(AgeRatingDTO expectedDTO, AgeRatingDTO resultDTO) {
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

    static boolean assertAgeRatingEntity(AgeRatingDTO expectedDTO, AgeRating resultDTO) {
        if (expectedDTO == null) {
            assert resultDTO == null
            return true
        }
        assert resultDTO != null
        assert resultDTO.getPlayniteId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }
}
