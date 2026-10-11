package pl.yalgrin.playnite.simplesync.util.panel

import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.panel.dto.PanelLibraryPluginDTO

import java.util.concurrent.ThreadLocalRandom

class PanelLibraryPluginFactoryUtil {
    static PanelLibraryPluginDTO createLibraryPlugin(Long id, String name) {
        return new PanelLibraryPluginDTO(id, name, false, false, null, null, false, false)
    }

    static PanelLibraryPluginDTO createLibraryPlugin(String name) {
        return createLibraryPlugin(null, name)
    }

    static PanelLibraryPluginDTO randomLibraryPlugin() {
        return createLibraryPlugin(UUID.randomUUID().toString())
    }

    static PanelLibraryPluginDTO pluginWithIndex(int idx) {
        return createLibraryPlugin("plugin-$idx")
    }

    static MockMultipartFile randomFile(String name, int size = 4096) {
        byte[] arr = new byte[size]
        ThreadLocalRandom.current().nextBytes(arr)
        new MockMultipartFile(name, name, MediaType.APPLICATION_OCTET_STREAM_VALUE, arr)
    }

    static List<MultipartFile> randomFiles(int iconChance = 80, int backgroundImageChance = 50) {
        def random = ThreadLocalRandom.current()
        List<MultipartFile> result = new ArrayList<>()
        if (random.nextInt(100) < iconChance) {
            result.add(randomFile("Icon.ico", 1024 + random.nextInt(3072)))
        }
        if (random.nextInt(100) < backgroundImageChance) {
            result.add(randomFile("BackgroundImage.jpeg", 2048 + random.nextInt(8192)))
        }
        return result
    }
}
