package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.GenreDTO

class GenreFactoryUtil {
    static GenreDTO createGenre(String id, String name, boolean removed = false) {
        return new GenreDTO(id, name, removed)
    }

    static GenreDTO randomGenre() {
        return createGenre(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static GenreDTO genreWithIndex(int idx) {
        return createGenre("id-$idx", "name-$idx")
    }
}
