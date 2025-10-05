package pl.yalgrin.playnite.simplesync.util

import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.dto.GameDTO
import pl.yalgrin.playnite.simplesync.dto.LinkDTO

import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.concurrent.ThreadLocalRandom

class GameFactoryUtil {
    static GameDTO createGame(String id, String name, boolean removed = false) {
        return GameDTO.builder()
                .id(id)
                .gameId(UUID.randomUUID().toString())
                .pluginId(UUID.randomUUID().toString())
                .name(name)
                .description("description")
                .notes("notes")
                .genres(List.of(
                        GenreFactoryUtil.createGenre(UUID.randomUUID().toString(), "genre-1"),
                        GenreFactoryUtil.createGenre(UUID.randomUUID().toString(), "genre-2")
                ))
                .hidden(true)
                .favorite(true)
                .lastActivity(ZonedDateTime.now())
                .sortingName("sorting-name")
                .platforms(List.of(
                        PlatformFactoryUtil.createPlatform(UUID.randomUUID().toString(), "platform-1"),
                        PlatformFactoryUtil.createPlatform(UUID.randomUUID().toString(), "platform-2")
                ))
                .publishers(List.of(
                        CompanyFactoryUtil.createCompany("company-1", "company-1"),
                        CompanyFactoryUtil.createCompany(UUID.randomUUID().toString(), "company-2"),
                        CompanyFactoryUtil.createCompany(UUID.randomUUID().toString(), "company-3")
                ))
                .developers(List.of(
                        CompanyFactoryUtil.createCompany("company-1", "company-1"),
                        CompanyFactoryUtil.createCompany(UUID.randomUUID().toString(), "company-4"),
                        CompanyFactoryUtil.createCompany(UUID.randomUUID().toString(), "company-5")
                ))
                .releaseDate(LocalDateTime.now())
                .categories(List.of(
                        CategoryFactoryUtil.createCategory(UUID.randomUUID().toString(), "category-1"),
                        CategoryFactoryUtil.createCategory(UUID.randomUUID().toString(), "category-2")
                ))
                .tags(List.of(
                        TagFactoryUtil.createTag(UUID.randomUUID().toString(), "tag-1"),
                        TagFactoryUtil.createTag(UUID.randomUUID().toString(), "tag-2")
                ))
                .features(List.of(
                        FeatureFactoryUtil.createFeature(UUID.randomUUID().toString(), "feature-1"),
                        FeatureFactoryUtil.createFeature(UUID.randomUUID().toString(), "feature-2")
                ))
                .links(List.of(
                        LinkDTO.builder().name("home page").url("https://some-website.com").build(),
                        LinkDTO.builder().name("steam page").url("https://steam.com/whatever").build()
                ))
                .playtime(12345678L)
                .added(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .playCount(321L)
                .lastSizeScanDate(ZonedDateTime.now())
                .series(List.of(
                        SeriesFactoryUtil.createSeries(UUID.randomUUID().toString(), "series-1"),
                        SeriesFactoryUtil.createSeries(UUID.randomUUID().toString(), "series-2")
                ))
                .version("1.0")
                .ageRatings(List.of(
                        AgeRatingFactoryUtil.createAgeRating(UUID.randomUUID().toString(), "pegi-13"),
                        AgeRatingFactoryUtil.createAgeRating(UUID.randomUUID().toString(), "pegi-18")
                ))
                .regions(List.of(
                        RegionFactoryUtil.createRegion(UUID.randomUUID().toString(), "region-1"),
                        RegionFactoryUtil.createRegion(UUID.randomUUID().toString(), "region-2")
                ))
                .source(SourceFactoryUtil.createSource(UUID.randomUUID().toString(), "steam"))
                .completionStatus(CompletionStatusFactoryUtil.createCompletionStatus(UUID.randomUUID().toString(), "completed"))
                .userScore(69)
                .criticScore(96)
                .communityScore(21)
                .manual("manual")
                .removed(removed)
                .build()
    }

    static GameDTO randomGame() {
        def random = ThreadLocalRandom.current()
        return GameDTO.builder()
                .id(UUID.randomUUID().toString())
                .gameId(UUID.randomUUID().toString())
                .pluginId(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .description(UUID.randomUUID().toString())
                .notes(UUID.randomUUID().toString())
                .genres(RandomUtil.generateRandomList { GenreFactoryUtil.randomGenre() })
                .hidden(random.nextBoolean())
                .favorite(random.nextBoolean())
                .lastActivity(ZonedDateTime.now().plusSeconds(random.nextInt()))
                .build()
    }

    static GameDTO gameWithIndex(int idx) {
        return randomGame().toBuilder()
                .id("id-$idx")
                .name("name-$idx")
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
