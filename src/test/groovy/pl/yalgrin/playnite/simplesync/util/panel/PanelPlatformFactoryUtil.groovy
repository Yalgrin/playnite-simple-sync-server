package pl.yalgrin.playnite.simplesync.util.panel

import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.panel.dto.PanelPlatformDTO

import java.util.concurrent.ThreadLocalRandom

class PanelPlatformFactoryUtil {
    static PanelPlatformDTO createPlatform(Long id, String name) {
        return new PanelPlatformDTO(id, name, UUID.randomUUID().toString(), false, false, false, null, null, null, false, false, false)
    }

    static PanelPlatformDTO createPlatform(String name) {
        return createPlatform(null, name)
    }

    static PanelPlatformDTO randomPlatform() {
        return createPlatform(UUID.randomUUID().toString())
    }

    static PanelPlatformDTO platformWithIndex(int idx) {
        return createPlatform(UUID.randomUUID().toString())
    }

    static MockMultipartFile randomFile(String name, int size = 4096) {
        byte[] arr = new byte[size]
        ThreadLocalRandom.current().nextBytes(arr)
        new MockMultipartFile(name, name, MediaType.APPLICATION_OCTET_STREAM_VALUE, arr)
    }

    static List<MultipartFile> randomFiles(int iconChance = 80, int coverImageChance = 60, int backgroundImageChance = 50) {
        def random = ThreadLocalRandom.current()
        List<MultipartFile> result = new ArrayList<>()
        if (random.nextInt(100) < iconChance) {
            result.add(randomFile("Icon.ico", 1024 + random.nextInt(3072)))
        }
        if (random.nextInt(100) < coverImageChance) {
            result.add(randomFile("CoverImage.png", 2048 + random.nextInt(4096)))
        }
        if (random.nextInt(100) < backgroundImageChance) {
            result.add(randomFile("BackgroundImage.jpeg", 2048 + random.nextInt(8192)))
        }
        return result
    }
}
