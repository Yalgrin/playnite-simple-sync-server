package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.panel.dto.PanelSourceDTO

class PanelSourceFactoryUtil {
    static PanelSourceDTO createSource(Long id, String name) {
        return new PanelSourceDTO(id, null, name)
    }

    static PanelSourceDTO createSource(String name) {
        return new PanelSourceDTO(null, null, name)
    }

    static PanelSourceDTO randomSource() {
        return createSource(UUID.randomUUID().toString())
    }

    static PanelSourceDTO sourceWithIndex(int idx) {
        return createSource("name-$idx")
    }
}
