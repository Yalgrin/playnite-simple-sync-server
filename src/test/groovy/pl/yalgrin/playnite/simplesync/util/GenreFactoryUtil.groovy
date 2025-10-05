package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.dto.GenreDTO

class GenreFactoryUtil {
    static GenreDTO createGenre(String id, String name, boolean removed = false) {
        return GenreDTO.builder()
                .id(id)
                .name(name)
                .removed(removed)
                .build()
    }

    static GenreDTO randomGenre() {
        return GenreDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .build()
    }

    static GenreDTO genreWithIndex(int idx) {
        return GenreDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .build()
    }
}
