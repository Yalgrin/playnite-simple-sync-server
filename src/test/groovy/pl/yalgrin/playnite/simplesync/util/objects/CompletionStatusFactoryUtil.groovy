package pl.yalgrin.playnite.simplesync.util.objects

import pl.yalgrin.playnite.simplesync.dto.objects.CompletionStatusDTO

class CompletionStatusFactoryUtil {
    static CompletionStatusDTO createCompletionStatus(String id, String name, boolean removed = false) {
        return CompletionStatusDTO.builder()
                .id(id)
                .name(name)
                .removed(removed)
                .build()
    }

    static CompletionStatusDTO randomCompletionStatus() {
        return CompletionStatusDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .build()
    }

    static CompletionStatusDTO completionStatusWithIndex(int idx) {
        return CompletionStatusDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .build()
    }
}
