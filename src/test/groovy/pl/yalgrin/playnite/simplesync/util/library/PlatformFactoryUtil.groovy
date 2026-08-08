package pl.yalgrin.playnite.simplesync.util.library

import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDTO

import java.util.concurrent.ThreadLocalRandom

class PlatformFactoryUtil {
    static PlatformDTO createPlatform(String id, String name, boolean removed = false) {
        return new PlatformDTO(id, name, removed, UUID.randomUUID().toString(), false, false, false)
    }

    static PlatformDTO randomPlatform() {
        return createPlatform(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    static PlatformDTO platformWithIndex(int idx) {
        return createPlatform(UUID.randomUUID().toString(), UUID.randomUUID().toString())
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
