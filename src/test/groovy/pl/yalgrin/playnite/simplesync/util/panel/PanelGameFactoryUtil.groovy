package pl.yalgrin.playnite.simplesync.util.panel

import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.library.dto.LinkDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGameDTO
import pl.yalgrin.playnite.simplesync.util.RandomUtil

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.concurrent.ThreadLocalRandom

class PanelGameFactoryUtil {
    static PanelGameDTO createGame(String name) {
        return createGame(null, name)
    }

    static PanelGameDTO createGame(Long id, String name) {
        return new PanelGameDTO(
                id,
                null,
                name,
                null,
                PanelLibraryPluginFactoryUtil.createLibraryPlugin("Playnite"),
                LocalDateTime.now(),
                "sorting-name-$name",
                List.of(
                        PanelSeriesFactoryUtil.createSeries("series-1"),
                        PanelSeriesFactoryUtil.createSeries("series-2")
                ),
                List.of(
                        PanelPlatformFactoryUtil.createPlatform("platform-1"),
                        PanelPlatformFactoryUtil.createPlatform("platform-2")
                ),
                List.of(
                        PanelAgeRatingFactoryUtil.createAgeRating("pegi-13"),
                        PanelAgeRatingFactoryUtil.createAgeRating("pegi-18")
                ),
                List.of(
                        PanelGenreFactoryUtil.createGenre("genre-1"),
                        PanelGenreFactoryUtil.createGenre("genre-2")
                ),
                List.of(
                        PanelRegionFactoryUtil.createRegion("region-1"),
                        PanelRegionFactoryUtil.createRegion("region-2")
                ),
                List.of(
                        PanelCompanyFactoryUtil.createCompany("company-1"),
                        PanelCompanyFactoryUtil.createCompany("company-2"),
                        PanelCompanyFactoryUtil.createCompany("company-3")
                ),
                PanelSourceFactoryUtil.createSource("steam"),
                List.of(
                        PanelCompanyFactoryUtil.createCompany("company-1"),
                        PanelCompanyFactoryUtil.createCompany("company-4"),
                        PanelCompanyFactoryUtil.createCompany("company-5")
                ),
                PanelCompletionStatusFactoryUtil.createCompletionStatus("completed"),
                List.of(
                        PanelCategoryFactoryUtil.createCategory("category-1"),
                        PanelCategoryFactoryUtil.createCategory("category-2")
                ),
                69,
                List.of(
                        PanelFeatureFactoryUtil.createFeature("feature-1"),
                        PanelFeatureFactoryUtil.createFeature("feature-2")
                ),
                96,
                List.of(
                        PanelTagFactoryUtil.createTag("tag-1"),
                        PanelTagFactoryUtil.createTag("tag-2")
                ),
                21,
                "description",
                ZonedDateTime.now(),
                43252345435L,
                321L,
                45891L,
                "1.0",
                "manual",
                true,
                true,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                "notes",
                List.of(
                        new LinkDTO("home page", "https://some-website.com"),
                        new LinkDTO("steam page", "https://steam.com/whatever")
                ),
                null,
                null,
                null,
                true,
                true,
                true
        )
    }

    static PanelGameDTO randomGame() {
        def random = ThreadLocalRandom.current()
        return new PanelGameDTO(
                null,
                null,
                UUID.randomUUID().toString(),
                null,
                PanelLibraryPluginFactoryUtil.createLibraryPlugin("Playnite"),
                LocalDateTime.now().plusDays(random.nextInt(100) - 50),
                UUID.randomUUID().toString(),
                RandomUtil.generateRandomList { PanelSeriesFactoryUtil.randomSeries() },
                RandomUtil.generateRandomList { PanelPlatformFactoryUtil.randomPlatform() },
                RandomUtil.generateRandomList { PanelAgeRatingFactoryUtil.randomAgeRating() },
                RandomUtil.generateRandomList { PanelGenreFactoryUtil.randomGenre() },
                RandomUtil.generateRandomList { PanelRegionFactoryUtil.randomRegion() },
                RandomUtil.generateRandomList { PanelCompanyFactoryUtil.randomCompany() },
                PanelSourceFactoryUtil.randomSource(),
                RandomUtil.generateRandomList { PanelCompanyFactoryUtil.randomCompany() },
                PanelCompletionStatusFactoryUtil.createCompletionStatus("completed"),
                RandomUtil.generateRandomList { PanelCategoryFactoryUtil.randomCategory() },
                random.nextInt(100),
                RandomUtil.generateRandomList { PanelFeatureFactoryUtil.randomFeature() },
                random.nextInt(100),
                RandomUtil.generateRandomList { PanelTagFactoryUtil.randomTag() },
                random.nextInt(100),
                UUID.randomUUID().toString(),
                ZonedDateTime.now(),
                random.nextLong(1_000_000_000),
                random.nextLong(1_000),
                random.nextLong(1_000_000),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                random.nextBoolean(),
                random.nextBoolean(),
                ZonedDateTime.now().plusSeconds(random.nextLong(1_000_000_000) - 500_000_000),
                ZonedDateTime.now().plusSeconds(random.nextLong(1_000_000_000) - 500_000_000),
                UUID.randomUUID().toString(),
                RandomUtil.generateRandomList { new LinkDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()) },
                null,
                null,
                null,
                random.nextBoolean(),
                random.nextBoolean(),
                random.nextBoolean()
        )
    }

    static PanelGameDTO gameWithIndex(int idx) {
        def game = randomGame()
        game.name = "name-$idx"
        return game
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
