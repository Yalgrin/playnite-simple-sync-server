package pl.yalgrin.playnite.simplesync.util.objects

import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.dto.objects.PlatformDTO

import java.util.concurrent.ThreadLocalRandom

class PlatformFactoryUtil {
    static PlatformDTO createPlatform(String id, String name, boolean removed = false) {
        return PlatformDTO.builder()
                .id(id)
                .name(name)
                .specificationId(UUID.randomUUID().toString())
                .removed(removed)
                .build()
    }

    static PlatformDTO randomPlatform() {
        return PlatformDTO.builder()
                .id(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .specificationId(UUID.randomUUID().toString())
                .build()
    }

    static PlatformDTO platformWithIndex(int idx) {
        return PlatformDTO.builder()
                .id("id-$idx")
                .name("name-$idx")
                .specificationId(UUID.randomUUID().toString())
                .build()
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
