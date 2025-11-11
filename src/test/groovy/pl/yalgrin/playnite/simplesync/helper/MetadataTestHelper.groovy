package pl.yalgrin.playnite.simplesync.helper

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.nio.file.Files
import java.nio.file.Path

@Component
class MetadataTestHelper {
    @Value("\${application.metadata-folder}")
    private String metadataFolder

    boolean fileExists(String objectType, Long id, String fileName) {
        def path = Path.of(metadataFolder, objectType, id + "", fileName)
        return Files.exists(path)
    }

    boolean fileDoesNotExist(String objectType, Long id, String fileName) {
        return !fileExists(objectType, id, fileName)
    }
}
