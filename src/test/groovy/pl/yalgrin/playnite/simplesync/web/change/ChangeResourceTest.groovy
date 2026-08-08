package pl.yalgrin.playnite.simplesync.web.change

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
import pl.yalgrin.playnite.simplesync.change.dto.GameChangeRequestDTO
import pl.yalgrin.playnite.simplesync.change.dto.GameIdsDTO
import pl.yalgrin.playnite.simplesync.change.repository.ChangeRepository
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationRequestDTO
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.client.service.RegisteredClientService
import pl.yalgrin.playnite.simplesync.common.config.ConstantsKt
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.dto.GameDTO
import pl.yalgrin.playnite.simplesync.security.SessionManager
import pl.yalgrin.playnite.simplesync.util.library.*
import reactor.test.StepVerifier

class ChangeResourceTest extends SpockIntegrationTest {
    @Autowired
    private ConnectionFactory connectionFactory

    @Autowired
    private ChangeRepository changeRepository
    @Autowired
    private RegisteredClientService registeredClientService
    @Autowired
    private SessionManager sessionManager

    private GameDTO savedGame
    private String clientId
    private String savedClientId

    def setup() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-data.sql"))
        populator.populate(connectionFactory).block()

        def clientInfo = registeredClientService.register(new RegistrationRequestDTO("test-user", ConstantsKt.CURRENT_API_VERSION)).block()
        def clientInfoToSaveData = registeredClientService.register(new RegistrationRequestDTO("other-user", ConstantsKt.CURRENT_API_VERSION)).block()
        def sessionId = UUID.randomUUID().toString()
        def otherUserSessionId = UUID.randomUUID().toString()
        sessionManager.saveSessionInfo(new SessionInfoDTO(clientInfo.clientId, clientInfo.displayName, sessionId))
        sessionManager.saveSessionInfo(new SessionInfoDTO(clientInfoToSaveData.clientId, clientInfoToSaveData.displayName, otherUserSessionId))

        def previousClient = webTestClient
        webTestClient = webTestClient.mutate()
                .defaultHeader("X-Client-Id", clientInfoToSaveData.clientId)
                .defaultHeader("X-Client-Token", clientInfoToSaveData.clientToken)
                .defaultHeader("X-Session-Id", otherUserSessionId)
                .build()

        clientId = clientInfo.clientId
        savedClientId = clientInfoToSaveData.clientId

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

        def game = GameFactoryUtil.randomGame()
        game.setCategories(List.of(category))
        game.setPlatforms(List.of(platform))
        game.setGenres(List.of(genre))
        game.setDevelopers(List.of(developer))
        game.setPublishers(List.of(publisher))
        game.setFeatures(List.of(feature))
        game.setTags(List.of(tag))
        game.setSeries(List.of(series))
        game.setAgeRatings(List.of(ageRating))
        game.setRegions(List.of(region))
        game.setSource(source)
        game.setCompletionStatus(completionStatus)
        savedGame = game

        makeSaveRequest(savedGame, "/api/game", List.of())

        webTestClient = previousClient.mutate()
                .defaultHeader("X-Client-Id", clientInfo.clientId)
                .defaultHeader("X-Client-Token", clientInfo.clientToken)
                .defaultHeader("X-Session-Id", sessionId)
                .build()
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
            assert savedClientId == change.getClientId()
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

    def "should generate changes for one game using UUID"() {
        given:
        def expectedResult = getResultsForGame()

        when:
        def result = generateChangesForGame(new GameChangeRequestDTO(List.of(), List.of(new GameIdsDTO(savedGame.getGameId(), savedGame.getPluginId()))))

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

    def "should generate changes for one game using raw ID"() {
        given:
        def expectedResult = getResultsForGame()

        when:
        def result = generateChangesForGame(new GameChangeRequestDTO(List.of(savedGame.getId()), List.of()))

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

    protected List<ChangeMessage> getAllExpectedResults() {
        List.of(
                new ChangeMessage(1L, ObjectType.CATEGORY, clientId, 1, false),
                new ChangeMessage(2L, ObjectType.GENRE, clientId, 1, false),
                new ChangeMessage(3L, ObjectType.PLATFORM, clientId, 1, false),
                new ChangeMessage(4L, ObjectType.COMPANY, clientId, 1, false),
                new ChangeMessage(5L, ObjectType.COMPANY, clientId, 2, false),
                new ChangeMessage(6L, ObjectType.FEATURE, clientId, 1, false),
                new ChangeMessage(7L, ObjectType.TAG, clientId, 1, false),
                new ChangeMessage(8L, ObjectType.SERIES, clientId, 1, false),
                new ChangeMessage(9L, ObjectType.AGE_RATING, clientId, 1, false),
                new ChangeMessage(10L, ObjectType.REGION, clientId, 1, false),
                new ChangeMessage(11L, ObjectType.SOURCE, clientId, 1, false),
                new ChangeMessage(12L, ObjectType.COMPLETION_STATUS, clientId, 1, false),
                new ChangeMessage(13L, ObjectType.FILTER_PRESET, clientId, 1, false),
                new ChangeMessage(14L, ObjectType.GAME, clientId, 1, false)
        )
    }

    protected List<ChangeMessage> getFilteredResults(long fromId) {
        getAllExpectedResults().stream().filter { it.id > fromId }.toList()
    }

    protected List<ChangeMessage> getResultsForGame() {
        getAllExpectedResults().stream().filter { it.type != ObjectType.FILTER_PRESET }.toList()
    }

    private WebTestClient.ResponseSpec makeSaveRequest(Object dto, String uri) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri}/save")
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
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
    }

    private List<ChangeMessage> fetchChanges(Long fromId) {
        return webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/change")
                        .queryParam("lastChangeId", fromId)
                        .build())
                .exchange()
                .returnResult(ChangeMessage.class).responseBody.collectList().block()
    }

    private List<ChangeMessage> generateChanges() {
        return webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/change/all")
                        .build())
                .exchange()
                .returnResult(ChangeMessage.class).responseBody.collectList().block()
    }

    private List<ChangeMessage> generateChangesForGame(GameChangeRequestDTO requestDTO) {
        return webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/change/games")
                        .build())
                .bodyValue(requestDTO)
                .exchange()
                .returnResult(ChangeMessage.class).responseBody.collectList().block()
    }
}
