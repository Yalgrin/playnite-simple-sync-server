package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.panel.dto.PanelGenreDTO

class PanelGenreFactoryUtil {
    static PanelGenreDTO createGenre(Long id, String name) {
        return new PanelGenreDTO(id, null, name)
    }

    static PanelGenreDTO createGenre(String name) {
        return new PanelGenreDTO(null, null, name)
    }

    static PanelGenreDTO randomGenre() {
        return createGenre(UUID.randomUUID().toString())
    }

    static PanelGenreDTO genreWithIndex(int idx) {
        return createGenre("name-$idx")
    }
}
