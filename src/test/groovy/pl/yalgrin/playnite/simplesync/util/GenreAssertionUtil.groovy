package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.domain.Genre
import pl.yalgrin.playnite.simplesync.dto.GenreDTO

class GenreAssertionUtil {
    static boolean assertGenre(GenreDTO expectedDTO, GenreDTO resultDTO) {
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

    static boolean assertGenreEntity(GenreDTO expectedDTO, Genre resultEntity) {
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
