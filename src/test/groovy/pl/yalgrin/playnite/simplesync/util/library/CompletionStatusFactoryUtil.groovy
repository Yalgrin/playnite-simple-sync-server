package pl.yalgrin.playnite.simplesync.util.library

import pl.yalgrin.playnite.simplesync.library.dto.CompletionStatusDTO

class CompletionStatusFactoryUtil {
    static CompletionStatusDTO createCompletionStatus(String id, String name, boolean removed = false) {
        return new CompletionStatusDTO(id, name, removed)
    }

    static CompletionStatusDTO randomCompletionStatus() {
        return createCompletionStatus(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static CompletionStatusDTO completionStatusWithIndex(int idx) {
        return createCompletionStatus("id-$idx", "name-$idx")
    }
}
