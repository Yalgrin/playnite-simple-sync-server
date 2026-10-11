package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.library.domain.Genre
import pl.yalgrin.playnite.simplesync.library.dto.GenreDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGenreDTO

class PanelGenreAssertionUtil {
    static boolean assertGenre(PanelGenreDTO expectedDTO, PanelGenreDTO resultDTO) {
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

    static boolean assertGenre(PanelGenreDTO expectedDTO, GenreDTO resultDTO) {
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

    static boolean assertGenreEntity(PanelGenreDTO expectedDTO, Genre resultDTO) {
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
