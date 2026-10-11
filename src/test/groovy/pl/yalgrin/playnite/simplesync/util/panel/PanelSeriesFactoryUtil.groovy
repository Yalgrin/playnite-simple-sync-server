package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.panel.dto.PanelSeriesDTO

class PanelSeriesFactoryUtil {
    static PanelSeriesDTO createSeries(Long id, String name) {
        return new PanelSeriesDTO(id, name)
    }

    static PanelSeriesDTO createSeries(String name) {
        return new PanelSeriesDTO(null, name)
    }

    static PanelSeriesDTO randomSeries() {
        return createSeries(UUID.randomUUID().toString())
    }

    static PanelSeriesDTO seriesWithIndex(int idx) {
        return createSeries("name-$idx")
    }
}
