package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.panel.dto.PanelTagDTO

class PanelTagFactoryUtil {
    static PanelTagDTO createTag(Long id, String name) {
        return new PanelTagDTO(id, null, name)
    }

    static PanelTagDTO createTag(String name) {
        return new PanelTagDTO(null, null, name)
    }

    static PanelTagDTO randomTag() {
        return createTag(UUID.randomUUID().toString())
    }

    static PanelTagDTO tagWithIndex(int idx) {
        return createTag("name-$idx")
    }
}
