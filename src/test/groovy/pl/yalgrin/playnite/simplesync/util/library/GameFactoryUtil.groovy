package pl.yalgrin.playnite.simplesync.util.library

import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.library.dto.GameDTO
import pl.yalgrin.playnite.simplesync.library.dto.LinkDTO
import pl.yalgrin.playnite.simplesync.util.RandomUtil

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.concurrent.ThreadLocalRandom

class GameFactoryUtil {
    static GameDTO createGame(String id, String name, boolean removed = false) {
        return new GameDTO(
                id,
                name,
                removed,
                "description",
                "notes",
                List.of(
                        GenreFactoryUtil.createGenre(UUID.randomUUID().toString(), "genre-1"),
                        GenreFactoryUtil.createGenre(UUID.randomUUID().toString(), "genre-2")
                ),
                true,
                true,
                ZonedDateTime.now(),
                "sorting-name",
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                List.of(
                        PlatformFactoryUtil.createPlatform(UUID.randomUUID().toString(), "platform-1"),
                        PlatformFactoryUtil.createPlatform(UUID.randomUUID().toString(), "platform-2")
                ),
                List.of(
                        CompanyFactoryUtil.createCompany("company-1", "company-1"),
                        CompanyFactoryUtil.createCompany(UUID.randomUUID().toString(), "company-2"),
                        CompanyFactoryUtil.createCompany(UUID.randomUUID().toString(), "company-3")
                ),
                List.of(
                        CompanyFactoryUtil.createCompany("company-1", "company-1"),
                        CompanyFactoryUtil.createCompany(UUID.randomUUID().toString(), "company-4"),
                        CompanyFactoryUtil.createCompany(UUID.randomUUID().toString(), "company-5")
                ),
                LocalDateTime.now(),
                List.of(
                        CategoryFactoryUtil.createCategory(UUID.randomUUID().toString(), "category-1"),
                        CategoryFactoryUtil.createCategory(UUID.randomUUID().toString(), "category-2")
                ),
                List.of(
                        TagFactoryUtil.createTag(UUID.randomUUID().toString(), "tag-1"),
                        TagFactoryUtil.createTag(UUID.randomUUID().toString(), "tag-2")
                ),
                List.of(
                        FeatureFactoryUtil.createFeature(UUID.randomUUID().toString(), "feature-1"),
                        FeatureFactoryUtil.createFeature(UUID.randomUUID().toString(), "feature-2")
                ),
                List.of(
                        new LinkDTO("home page", "https://some-website.com"),
                        new LinkDTO("steam page", "https://steam.com/whatever")
                ),
                12345678L,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                321L,
                43252345435L,
                ZonedDateTime.now(),
                List.of(
                        SeriesFactoryUtil.createSeries(UUID.randomUUID().toString(), "series-1"),
                        SeriesFactoryUtil.createSeries(UUID.randomUUID().toString(), "series-2")
                ),
                "1.0",
                List.of(
                        AgeRatingFactoryUtil.createAgeRating(UUID.randomUUID().toString(), "pegi-13"),
                        AgeRatingFactoryUtil.createAgeRating(UUID.randomUUID().toString(), "pegi-18")
                ),
                List.of(
                        RegionFactoryUtil.createRegion(UUID.randomUUID().toString(), "region-1"),
                        RegionFactoryUtil.createRegion(UUID.randomUUID().toString(), "region-2")
                ),
                SourceFactoryUtil.createSource(UUID.randomUUID().toString(), "steam"),
                CompletionStatusFactoryUtil.createCompletionStatus(UUID.randomUUID().toString(), "completed"),
                69,
                96,
                21,
                "manual",
                true,
                true,
                true
        )
    }

    static GameDTO randomGame() {
        def random = ThreadLocalRandom.current()
        return new GameDTO(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                false,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                RandomUtil.generateRandomList { GenreFactoryUtil.randomGenre() },
                random.nextBoolean(),
                random.nextBoolean(),
                ZonedDateTime.now().plusSeconds(random.nextInt()),
                null,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                0,
                null,
                null,
                0,
                0,
                null,
                List.of(),
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                false
        )
    }

    static GameDTO gameWithIndex(int idx) {
        return randomGame().withIdAndName("id-$idx", "name-$idx")
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
