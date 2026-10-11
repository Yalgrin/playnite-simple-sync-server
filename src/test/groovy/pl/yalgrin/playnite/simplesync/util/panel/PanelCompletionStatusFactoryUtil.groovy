package pl.yalgrin.playnite.simplesync.util.panel

import pl.yalgrin.playnite.simplesync.panel.dto.PanelCompletionStatusDTO

class PanelCompletionStatusFactoryUtil {
    static PanelCompletionStatusDTO createCompletionStatus(Long id, String name) {
        return new PanelCompletionStatusDTO(id, name)
    }

    static PanelCompletionStatusDTO createCompletionStatus(String name) {
        return new PanelCompletionStatusDTO(null, name)
    }

    static PanelCompletionStatusDTO randomCompletionStatus() {
        return createCompletionStatus(UUID.randomUUID().toString())
    }

    static PanelCompletionStatusDTO completionStatusWithIndex(int idx) {
        return createCompletionStatus("name-$idx")
    }
}
