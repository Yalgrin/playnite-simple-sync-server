package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.SeriesDTO

class SeriesFactoryUtil {
    static SeriesDTO createSeries(String id, String name, boolean removed = false) {
        return new SeriesDTO(id, name, removed)
    }

    static SeriesDTO randomSeries() {
        return createSeries(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static SeriesDTO seriesWithIndex(int idx) {
        return createSeries("id-$idx", "name-$idx")
    }
}
