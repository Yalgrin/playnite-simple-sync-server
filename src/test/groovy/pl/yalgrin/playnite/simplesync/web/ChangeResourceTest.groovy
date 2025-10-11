package pl.yalgrin.playnite.simplesync.web

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.dto.GameChangeRequestDTO
import pl.yalgrin.playnite.simplesync.dto.GameIdsDTO
import pl.yalgrin.playnite.simplesync.dto.objects.GameDTO
import pl.yalgrin.playnite.simplesync.enums.ObjectType
import pl.yalgrin.playnite.simplesync.repository.ChangeRepository
import pl.yalgrin.playnite.simplesync.util.objects.*
import reactor.test.StepVerifier

class ChangeResourceTest extends SpockIntegrationTest {
    @Autowired
    private ConnectionFactory connectionFactory

    @Autowired
    private ChangeRepository changeRepository

    private GameDTO savedGame

    def setup() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-data.sql"))
        populator.populate(connectionFactory).block()


        def category = CategoryFactoryUtil.randomCategory()
        makeSaveRequest(category, "/api/category")

        def genre = GenreFactoryUtil.randomGenre()
        makeSaveRequest(genre, "/api/genre")

        def platform = PlatformFactoryUtil.randomPlatform()
        makeSaveRequest(platform, "/api/platform", List.of())

        def developer = CompanyFactoryUtil.randomCompany()
        makeSaveRequest(developer, "/api/company")

        def publisher = CompanyFactoryUtil.randomCompany()
        makeSaveRequest(publisher, "/api/company")

        def feature = FeatureFactoryUtil.randomFeature()
        makeSaveRequest(feature, "/api/feature")

        def tag = TagFactoryUtil.randomTag()
        makeSaveRequest(tag, "/api/tag")

        def series = SeriesFactoryUtil.randomSeries()
        makeSaveRequest(series, "/api/series")

        def ageRating = AgeRatingFactoryUtil.randomAgeRating()
        makeSaveRequest(ageRating, "/api/age-rating")

        def region = RegionFactoryUtil.randomRegion()
        makeSaveRequest(region, "/api/region")

        def source = SourceFactoryUtil.randomSource()
        makeSaveRequest(source, "/api/source")

        def completionStatus = CompletionStatusFactoryUtil.randomCompletionStatus()
        makeSaveRequest(completionStatus, "/api/completion-status")

        def filterPreset = FilterPresetFactoryUtil.randomFilterPreset()
        makeSaveRequest(filterPreset, "/api/filter-preset")

        savedGame = GameFactoryUtil.randomGame().toBuilder()
                .categories(List.of(category))
                .platforms(List.of(platform))
                .genres(List.of(genre))
                .developers(List.of(developer))
                .publishers(List.of(publisher))
                .features(List.of(feature))
                .tags(List.of(tag))
                .series(List.of(series))
                .ageRatings(List.of(ageRating))
                .regions(List.of(region))
                .source(source)
                .completionStatus(completionStatus)
                .build()

        makeSaveRequest(savedGame, "/api/game", List.of())
    }

    def "last change id should be match"() {
        when:
        def result = changeRepository.findMaxId()

        then:
        StepVerifier.create(result)
                .expectNext(14L)
                .verifyComplete()

        when:
        makeSaveRequest(CategoryFactoryUtil.randomCategory(), "/api/category")
        def secondResult = changeRepository.findMaxId()

        then:
        StepVerifier.create(secondResult)
                .expectNext(15L)
                .verifyComplete()
    }

    def "should return changes from id"() {
        when:
        def result = fetchChanges(fromId)

        then:
        assert expectedResult.size() == result.size()
        for (def i = 0; i < expectedResult.size(); i++) {
            def expectedChange = expectedResult[i]
            def change = result[i]
            assert expectedChange.getId() == change.getId()
            assert expectedChange.getType() == change.getType()
            assert expectedChange.getClientId() == change.getClientId()
            assert expectedChange.getObjectId() == change.getObjectId()
        }
        true

        where:
        fromId || expectedResult
        null   || getAllExpectedResults()
        0      || getAllExpectedResults()
        3      || getFilteredResults(3)
        10     || getFilteredResults(10)
        20     || getFilteredResults(20)
    }

    def "should generate all changes"() {
        given:
        def expectedResult = getAllExpectedResults()

        when:
        def result = generateChanges()

        then:
        assert expectedResult.size() == result.size()
        for (def i = 0; i < expectedResult.size(); i++) {
            def expectedChange = expectedResult[i]
            def change = result[i]
            assert change.getId() == 14
            assert expectedChange.getType() == change.getType()
            assert change.getClientId() == null
            assert expectedChange.getObjectId() == change.getObjectId()
        }
        true
    }

    def "should generate changes for one game"() {
        given:
        def expectedResult = getResultsForGame()

        when:
        def result = generateChangesForGame(GameChangeRequestDTO.builder().gameIds(List.of(GameIdsDTO.builder().gameId(savedGame.getGameId()).pluginId(savedGame.getPluginId()).build())).build())

        then:
        assert expectedResult.size() == result.size()
        for (def i = 0; i < expectedResult.size(); i++) {
            def expectedChange = expectedResult[i]
            def change = result[i]
            assert change.getId() == null
            assert expectedChange.getType() == change.getType()
            assert change.getClientId() == null
            assert expectedChange.getObjectId() == change.getObjectId()
        }
        true
    }

    protected List<ChangeDTO> getAllExpectedResults() {
        List.of(
                ChangeDTO.builder().id(1L).type(ObjectType.Category).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(2L).type(ObjectType.Genre).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(3L).type(ObjectType.Platform).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(4L).type(ObjectType.Company).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(5L).type(ObjectType.Company).clientId("test").objectId(2).build(),
                ChangeDTO.builder().id(6L).type(ObjectType.Feature).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(7L).type(ObjectType.Tag).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(8L).type(ObjectType.Series).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(9L).type(ObjectType.AgeRating).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(10L).type(ObjectType.Region).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(11L).type(ObjectType.Source).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(12L).type(ObjectType.CompletionStatus).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(13L).type(ObjectType.FilterPreset).clientId("test").objectId(1).build(),
                ChangeDTO.builder().id(14L).type(ObjectType.Game).clientId("test").objectId(1).build()
        )
    }

    protected List<ChangeDTO> getFilteredResults(long fromId) {
        getAllExpectedResults().stream().filter { it.id > fromId }.toList()
    }

    protected List<ChangeDTO> getResultsForGame() {
        getAllExpectedResults().stream().filter { it.type != ObjectType.FilterPreset }.toList()
    }

    private WebTestClient.ResponseSpec makeSaveRequest(Object dto, String uri) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri}/save")
                        .queryParam("clientId", "test")
                        .build())
                .bodyValue(dto)
                .exchange()
    }

    private WebTestClient.ResponseSpec makeSaveRequest(Object dto, String uri, List<MultipartFile> files) {
        def builder = new MultipartBodyBuilder()
        builder.part("dto", dto)
        if (!files.isEmpty()) {
            files.each { file ->
                builder.part("files", new ByteArrayResource(file.getBytes()) {
                    @Override
                    String getFilename() {
                        return file.getOriginalFilename()
                    }
                })
            }
        }
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri}/save")
                        .queryParam("clientId", "test")
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
    }

    private List<ChangeDTO> fetchChanges(Long fromId) {
        return webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/change")
                        .queryParam("lastChangeId", fromId)
                        .build())
                .exchange()
                .returnResult(ChangeDTO.class).responseBody.collectList().block()
    }

    private List<ChangeDTO> generateChanges() {
        return webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/change/all")
                        .build())
                .exchange()
                .returnResult(ChangeDTO.class).responseBody.collectList().block()
    }

    private List<ChangeDTO> generateChangesForGame(GameChangeRequestDTO requestDTO) {
        return webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/change/games")
                        .build())
                .bodyValue(requestDTO)
                .exchange()
                .returnResult(ChangeDTO.class).responseBody.collectList().block()
    }
}
