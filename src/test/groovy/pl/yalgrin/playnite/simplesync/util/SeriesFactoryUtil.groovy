package pl.yalgrin.playnite.simplesync.util

import pl.yalgrin.playnite.simplesync.dto.SeriesDTO

class SeriesFactoryUtil {
    static SeriesDTO createSeries(String id, String name, boolean removed = false) {
        return SeriesDTO.builder()
                .id(id)
                .name(name)
                .removed(removed)
                .build()
    }

    static SeriesDTO randomSeries() {
        return SeriesDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .build()
    }

    static SeriesDTO seriesWithIndex(int idx) {
        return SeriesDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .build()
    }
}
