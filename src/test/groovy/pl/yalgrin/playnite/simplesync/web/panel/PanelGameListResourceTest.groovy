package pl.yalgrin.playnite.simplesync.web.panel

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
import pl.yalgrin.playnite.simplesync.client.dto.RegisteredClientDTO
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationRequestDTO
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.dto.SessionSettingsDTO
import pl.yalgrin.playnite.simplesync.client.service.RegisteredClientService
import pl.yalgrin.playnite.simplesync.library.dto.GameDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.IdItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.IntItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.dto.filter.StringItemPropertiesDTO
import pl.yalgrin.playnite.simplesync.library.enums.InstallSizeGroup
import pl.yalgrin.playnite.simplesync.library.enums.PastTimeSegment
import pl.yalgrin.playnite.simplesync.library.enums.PlaytimeCategory
import pl.yalgrin.playnite.simplesync.library.enums.ScoreGroup
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.library.service.MetadataService
import pl.yalgrin.playnite.simplesync.panel.dto.CsrfResponse
import pl.yalgrin.playnite.simplesync.panel.dto.GameListFilterDTO
import pl.yalgrin.playnite.simplesync.panel.dto.GameListResultDTO
import pl.yalgrin.playnite.simplesync.panel.dto.LoginRequest
import pl.yalgrin.playnite.simplesync.panel.repository.PanelUserRepository
import pl.yalgrin.playnite.simplesync.panel.service.PanelUserService
import pl.yalgrin.playnite.simplesync.security.SessionManager
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.library.*
import reactor.test.StepVerifier
import spock.lang.Shared
import tools.jackson.databind.json.JsonMapper

import java.time.LocalDateTime
import java.time.ZonedDateTime

import static pl.yalgrin.playnite.simplesync.common.config.ConstantsKt.*

class PanelGameListResourceTest extends SpockIntegrationTest {
    private static final String FIRST_GAME_ID = "1"
    private static final String SECOND_GAME_ID = "2"
    private static final String THIRD_GAME_ID = "3"
    private static final String FOURTH_GAME_ID = "4"

    @Shared
    @Autowired
    private ConnectionFactory connectionFactory
    @Shared
    @Autowired
    private RegisteredClientService registeredClientService
    @Shared
    @Autowired
    private SessionManager sessionManager
    @Shared
    @Autowired
    private PanelUserService userService
    @Shared
    @Autowired
    private PanelUserRepository userRepository
    @Shared
    @Autowired
    private MetadataService metadataService
    @Autowired
    protected JsonMapper jsonMapper
    @Shared
    @Autowired
    protected GameRepository gameRepository
    @Shared
    protected WebTestClient pluginWebTestClient

    @Shared
    protected RegisteredClientDTO otherClientInfo

    def setupSpec() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-db.sql"))
        populator.populate(connectionFactory).block()

        userService.init().block()
        metadataService.init()

        pluginWebTestClient = webTestClient

        def clientInfo = registeredClientService.register(new RegistrationRequestDTO("test-user", CURRENT_API_VERSION)).block()
        def sessionId = UUID.randomUUID().toString()
        sessionManager.saveSessionInfo(new SessionInfoDTO(clientInfo.clientId, clientInfo.displayName, sessionId, false))

        pluginWebTestClient = pluginWebTestClient.mutate()
                .defaultHeader("X-Client-Id", clientInfo.clientId)
                .defaultHeader("X-Client-Token", clientInfo.clientToken)
                .defaultHeader("X-Session-Id", sessionId)
                .build()

        def csrfResult = webTestClient.get()
                .uri("/api/panel/auth/csrf")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CsrfResponse)
                .returnResult()

        var session = csrfResult.getResponseCookies().getFirst("SESSION")

        webTestClient = webTestClient.mutate()
                .defaultCookie(session.getName(), session.getValue())
                .defaultHeader(csrfResult.responseBody.headerName, csrfResult.responseBody.token)
                .build()

        def login = webTestClient.post()
                .uri("/api/panel/auth/login")
                .bodyValue(new LoginRequest("admin", "admin", true))
                .exchange()
                .expectStatus().isOk()
                .returnResult()

        session = login.getResponseCookies().getFirst("SESSION")

        webTestClient = webTestClient.mutate()
                .defaultCookie(session.getName(), session.getValue())
                .build()

        csrfResult = webTestClient.get()
                .uri("/api/panel/auth/csrf")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CsrfResponse)
                .returnResult()

        session = csrfResult.getResponseCookies().getFirst("SESSION")

        webTestClient = webTestClient.mutate()
                .defaultCookie(session.getName(), session.getValue())
                .defaultHeader(csrfResult.responseBody.headerName, csrfResult.responseBody.token)
                .build()

        def user = userRepository.findAll().blockFirst()
        def clientId = user.id.toString()

        def panelSessionId = UUID.randomUUID().toString()

        def sessionInfo = new SessionInfoDTO(clientId, user.username, panelSessionId, true)
        sessionManager.saveSessionInfo(sessionInfo)
        sessionManager.updateSettings(sessionInfo, new SessionSettingsDTO(true))

        otherClientInfo = registeredClientService.register(new RegistrationRequestDTO("other-client", CURRENT_API_VERSION)).block()

        webTestClient = webTestClient.mutate()
                .defaultHeader("X-Session-Id", panelSessionId)
                .build()

        def game = GameFactoryUtil.randomGame()
        game.id = FIRST_GAME_ID
        game.hidden = false
        game.favorite = false
        game.name = "Game 1"
        game.version = "1.0"
        game.releaseDate = LocalDateTime.of(2026, 9, 11, 0, 0, 0)
        game.genres = List.of(GenreFactoryUtil.createGenre("aaa", "Genre 1"), GenreFactoryUtil.createGenre("bbb", "Genre 2"))
        game.platforms = List.of(PlatformFactoryUtil.createPlatform("aaa", "Platform 1"), PlatformFactoryUtil.createPlatform("bbb", "Platform 2"))
        game.publishers = List.of(CompanyFactoryUtil.createCompany("aaa", "Company 1"), CompanyFactoryUtil.createCompany("bbb", "Company 2"))
        game.developers = List.of(CompanyFactoryUtil.createCompany("aaa", "Company 1"), CompanyFactoryUtil.createCompany("bbb", "Company 2"))
        game.categories = List.of(CategoryFactoryUtil.createCategory("aaa", "Category 1"), CategoryFactoryUtil.createCategory("bbb", "Category 2"))
        game.tags = List.of(TagFactoryUtil.createTag("aaa", "Tag 1"), TagFactoryUtil.createTag("bbb", "Tag 2"))
        game.series = List.of(SeriesFactoryUtil.createSeries("aaa", "Series 1"), SeriesFactoryUtil.createSeries("bbb", "Series 2"))
        game.regions = List.of(RegionFactoryUtil.createRegion("aaa", "Region 1"), RegionFactoryUtil.createRegion("bbb", "Region 2"))
        game.source = SourceFactoryUtil.createSource("aaa", "Source 1")
        game.ageRatings = List.of(AgeRatingFactoryUtil.createAgeRating("aaa", "Rating 1"), AgeRatingFactoryUtil.createAgeRating("bbb", "Rating 2"))
        game.pluginId = "aaa"
        game.pluginName = "Plugin 1"
        game.completionStatus = CompletionStatusFactoryUtil.createCompletionStatus("aaa", "Status 1")
        game.features = List.of(FeatureFactoryUtil.createFeature("aaa", "Feature 1"), FeatureFactoryUtil.createFeature("bbb", "Feature 2"))
        game.userScore = 69
        game.communityScore = 88
        game.criticScore = 100
        game.lastActivity = ZonedDateTime.now()
        game.added = ZonedDateTime.now().minusDays(1)
        game.modified = ZonedDateTime.now()
        game.playtime = 2560
        game.installSize = 150_000_000
        makeSaveRequest(game, "/api/game", List.of())

        game = GameFactoryUtil.randomGame()
        game.id = SECOND_GAME_ID
        game.hidden = false
        game.favorite = true
        game.name = "Some other name"
        game.version = "2.1"
        game.releaseDate = LocalDateTime.of(2001, 11, 30, 0, 0, 0)
        game.genres = List.of(GenreFactoryUtil.createGenre("aaa", "Genre 1"), GenreFactoryUtil.createGenre("ccc", "Genre 3"))
        game.platforms = List.of(PlatformFactoryUtil.createPlatform("aaa", "Platform 1"), PlatformFactoryUtil.createPlatform("ccc", "Platform 3"))
        game.publishers = List.of(CompanyFactoryUtil.createCompany("aaa", "Company 1"), CompanyFactoryUtil.createCompany("ccc", "Company 3"))
        game.developers = List.of(CompanyFactoryUtil.createCompany("aaa", "Company 1"), CompanyFactoryUtil.createCompany("ccc", "Company 3"))
        game.categories = List.of(CategoryFactoryUtil.createCategory("aaa", "Category 1"), CategoryFactoryUtil.createCategory("ccc", "Category 3"))
        game.tags = List.of(TagFactoryUtil.createTag("aaa", "Tag 1"), TagFactoryUtil.createTag("ccc", "Tag 3"))
        game.series = List.of(SeriesFactoryUtil.createSeries("aaa", "Series 1"), SeriesFactoryUtil.createSeries("ccc", "Series 3"))
        game.regions = List.of(RegionFactoryUtil.createRegion("aaa", "Region 1"), RegionFactoryUtil.createRegion("ccc", "Region 3"))
        game.source = SourceFactoryUtil.createSource("aaa", "Source 1")
        game.ageRatings = List.of(AgeRatingFactoryUtil.createAgeRating("aaa", "Rating 1"), AgeRatingFactoryUtil.createAgeRating("ccc", "Rating 3"))
        game.pluginId = "aaa"
        game.pluginName = "Plugin 1"
        game.completionStatus = CompletionStatusFactoryUtil.createCompletionStatus("aaa", "Status 1")
        game.features = List.of(FeatureFactoryUtil.createFeature("aaa", "Feature 1"), FeatureFactoryUtil.createFeature("ccc", "Feature 3"))
        game.userScore = 5
        game.communityScore = null
        game.criticScore = 30
        game.lastActivity = ZonedDateTime.now().minusDays(6)
        game.added = ZonedDateTime.now().minusDays(25)
        game.modified = null
        game.playtime = 0
        game.installSize = 31_750_000_000
        makeSaveRequest(game, "/api/game", List.of())

        game = GameFactoryUtil.randomGame()
        game.id = THIRD_GAME_ID
        game.hidden = true
        game.favorite = false
        game.name = "xxx"
        game.version = "23.0"
        game.releaseDate = LocalDateTime.of(2002, 2, 2, 0, 0, 0)
        game.genres = List.of(GenreFactoryUtil.createGenre("ddd", "%\\_"))
        game.platforms = List.of(PlatformFactoryUtil.createPlatform("ddd", "%\\_"))
        game.publishers = List.of(CompanyFactoryUtil.createCompany("ddd", "%\\_"))
        game.developers = List.of(CompanyFactoryUtil.createCompany("ddd", "%\\_"))
        game.categories = List.of(CategoryFactoryUtil.createCategory("ddd", "%\\_"))
        game.tags = List.of(TagFactoryUtil.createTag("ddd", "%\\_"))
        game.series = List.of(SeriesFactoryUtil.createSeries("ddd", "%\\_"))
        game.regions = List.of(RegionFactoryUtil.createRegion("ddd", "%\\_"))
        game.source = SourceFactoryUtil.createSource("bbb", "%\\_")
        game.ageRatings = List.of(AgeRatingFactoryUtil.createAgeRating("ddd", "%\\_"))
        game.pluginId = "bbb"
        game.pluginName = "%\\_"
        game.completionStatus = CompletionStatusFactoryUtil.createCompletionStatus("bbb", "%\\_")
        game.features = List.of(FeatureFactoryUtil.createFeature("ddd", "%\\_"))
        game.userScore = 0
        game.communityScore = 99
        game.criticScore = 13
        game.lastActivity = ZonedDateTime.now().minusDays(400)
        game.added = ZonedDateTime.now().minusDays(280)
        game.modified = ZonedDateTime.now().minusDays(300)
        game.playtime = 2250000
        game.installSize = 0
        makeSaveRequest(game, "/api/game", List.of())

        game = new GameDTO()
        game.id = FOURTH_GAME_ID
        game.gameId = FOURTH_GAME_ID
        game.pluginId = NULL_GUID_ID
        makeSaveRequest(game, "/api/game", List.of())
    }

    def "should filter games by hidden"() {
        given:
        def filter = new GameListFilterDTO()
        filter.hidden = hidden

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        hidden || foundIds
        null  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        false || [FIRST_GAME_ID, SECOND_GAME_ID, FOURTH_GAME_ID]
        true   || [THIRD_GAME_ID]
    }

    def "should filter games by favorite"() {
        given:
        def filter = new GameListFilterDTO()
        filter.favorite = favorite

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        favorite || foundIds
        null  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        true     || [SECOND_GAME_ID]
    }

    def "should filter games by name"() {
        given:
        def filter = new GameListFilterDTO()
        filter.name = name

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        name              || foundIds
        null || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        ""   || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "a"               || [FIRST_GAME_ID, SECOND_GAME_ID]
        "x"               || [THIRD_GAME_ID]
        "game"            || [FIRST_GAME_ID]
        "Some other name" || [SECOND_GAME_ID]
        "something else"  || []
    }

    def "should filter games by version"() {
        given:
        def filter = new GameListFilterDTO()
        filter.version = version

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        version || foundIds
        null || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        ""   || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "."     || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ".0"    || [FIRST_GAME_ID, THIRD_GAME_ID]
        "1.0"   || [FIRST_GAME_ID]
        "1"     || [FIRST_GAME_ID, SECOND_GAME_ID]
        "2"     || [SECOND_GAME_ID, THIRD_GAME_ID]
        "2.1"   || [SECOND_GAME_ID]
        "2.2"   || []
    }

    def "should filter games by release year"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new StringItemPropertiesDTO()
        fieldFilter.values = releaseYears
        filter.releaseYear = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        releaseYears     || foundIds
        []               || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]             || [FOURTH_GAME_ID]
        [NULL_OBJECT_ID] || [FOURTH_GAME_ID]
        ["2001"]         || [SECOND_GAME_ID]
        ["2001", "2026"] || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["2026"]         || [FIRST_GAME_ID]
        ["2003"]         || []
        ["2002", "2001"] || [SECOND_GAME_ID, THIRD_GAME_ID]
    }

    def "should filter games by genre ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.genre = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by genre text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.genre = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Genre"    || [FIRST_GAME_ID, SECOND_GAME_ID]
        "re 1"     || [FIRST_GAME_ID, SECOND_GAME_ID]
        "eNrE 2"   || [FIRST_GAME_ID]
        "eNrE 3"   || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by platform ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.platform = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by platform text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.platform = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Platform" || [FIRST_GAME_ID, SECOND_GAME_ID]
        "rm 1"     || [FIRST_GAME_ID, SECOND_GAME_ID]
        "fOrM 2"   || [FIRST_GAME_ID]
        "FoRm 3"   || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by publisher ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.publisher = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by publisher text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.publisher = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Company"  || [FIRST_GAME_ID, SECOND_GAME_ID]
        "ny 1"     || [FIRST_GAME_ID, SECOND_GAME_ID]
        "PaNy 2"   || [FIRST_GAME_ID]
        "pAnY 3"   || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by developer ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.developer = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by developer text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.developer = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Company"  || [FIRST_GAME_ID, SECOND_GAME_ID]
        "ny 1"     || [FIRST_GAME_ID, SECOND_GAME_ID]
        "PaNy 2"   || [FIRST_GAME_ID]
        "pAnY 3"   || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by category ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.category = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by category text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.category = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Category" || [FIRST_GAME_ID, SECOND_GAME_ID]
        "ry 1"     || [FIRST_GAME_ID, SECOND_GAME_ID]
        "GoRy 2"   || [FIRST_GAME_ID]
        "gOrY 3"   || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by tag ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.tag = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by tag text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.tag = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Tag"      || [FIRST_GAME_ID, SECOND_GAME_ID]
        "g 1"      || [FIRST_GAME_ID, SECOND_GAME_ID]
        "aG 2"     || [FIRST_GAME_ID]
        "Ag 3"     || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by series ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.series = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by series text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.series = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Series"   || [FIRST_GAME_ID, SECOND_GAME_ID]
        "ies 1"    || [FIRST_GAME_ID, SECOND_GAME_ID]
        "RiEs 2"   || [FIRST_GAME_ID]
        "rIeS 3"   || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by region ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.region = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by region text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.region = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Region"   || [FIRST_GAME_ID, SECOND_GAME_ID]
        "ion 1"    || [FIRST_GAME_ID, SECOND_GAME_ID]
        "GiOn 2"   || [FIRST_GAME_ID]
        "gIoN 3"   || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by source ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.source = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [THIRD_GAME_ID]
        ["ccc"]               | false        || []
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [THIRD_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [THIRD_GAME_ID]
        ["ccc"]               | true         || []
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["aaa", "ccc"]        | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || [THIRD_GAME_ID]
        ["aaa", "bbb", "ccc"] | true         || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["invalid"]           | true         || []
    }

    def "should filter games by source text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.source = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Source"   || [FIRST_GAME_ID, SECOND_GAME_ID]
        "rce 1"    || [FIRST_GAME_ID, SECOND_GAME_ID]
        "RcE 1"    || [FIRST_GAME_ID, SECOND_GAME_ID]
        "rce 2"    || []
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by age rating ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.ageRating = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by age rating text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.ageRating = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Rating"   || [FIRST_GAME_ID, SECOND_GAME_ID]
        "ing 1"    || [FIRST_GAME_ID, SECOND_GAME_ID]
        "TiNG 2"   || [FIRST_GAME_ID]
        "tInG 3"   || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by library ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.library = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [THIRD_GAME_ID]
        ["ccc"]               | false        || []
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [THIRD_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [THIRD_GAME_ID]
        ["ccc"]               | true         || []
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["aaa", "ccc"]        | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || [THIRD_GAME_ID]
        ["aaa", "bbb", "ccc"] | true         || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["invalid"]           | true         || []
    }

    def "should filter games by library text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.library = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Plugin"   || [FIRST_GAME_ID, SECOND_GAME_ID]
        "gIn 1"    || [FIRST_GAME_ID, SECOND_GAME_ID]
        "UgIN 1"   || [FIRST_GAME_ID, SECOND_GAME_ID]
        "in 2"     || []
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by completion status ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.completionStatuses = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [THIRD_GAME_ID]
        ["ccc"]               | false        || []
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [THIRD_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [THIRD_GAME_ID]
        ["ccc"]               | true         || []
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["aaa", "ccc"]        | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || [THIRD_GAME_ID]
        ["aaa", "bbb", "ccc"] | true         || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
        ["invalid"]           | true         || []
    }

    def "should filter games by completion status text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.completionStatuses = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Status"   || [FIRST_GAME_ID, SECOND_GAME_ID]
        "us 1"     || [FIRST_GAME_ID, SECOND_GAME_ID]
        "aTUs 1"   || [FIRST_GAME_ID, SECOND_GAME_ID]
        "us 2"     || []
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by feature ids"() {
        given:
        def filter = new GameListFilterDTO()
        filter.useAndFilteringStyle = useAndFilter
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.ids = filterIds
        filter.feature = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterIds             | useAndFilter || foundIds
        []             | false || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | false || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | false || [FOURTH_GAME_ID]
        ["aaa"]               | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | false        || [FIRST_GAME_ID]
        ["ccc"]               | false        || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb", "ccc"]        | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["aaa", "bbb", "ccc"] | false        || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["invalid"]           | false        || []
        []             | true  || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [""]           | true  || [FOURTH_GAME_ID]
        [NULL_GUID_ID] | true  || [FOURTH_GAME_ID]
        ["aaa"]               | true         || [FIRST_GAME_ID, SECOND_GAME_ID]
        ["bbb"]               | true         || [FIRST_GAME_ID]
        ["ccc"]               | true         || [SECOND_GAME_ID]
        ["aaa", "bbb"]        | true         || [FIRST_GAME_ID]
        ["aaa", "ccc"]        | true         || [SECOND_GAME_ID]
        ["bbb", "ccc"]        | true         || []
        ["aaa", "bbb", "ccc"] | true         || []
        ["invalid"]           | true         || []
    }

    def "should filter games by feature text"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IdItemPropertiesDTO()
        fieldFilter.text = filterText
        filter.feature = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterText || foundIds
        "" || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        "Feature"  || [FIRST_GAME_ID, SECOND_GAME_ID]
        "ure 1"    || [FIRST_GAME_ID, SECOND_GAME_ID]
        "TuRe 2"   || [FIRST_GAME_ID]
        "tUrE 3"   || [SECOND_GAME_ID]
        "whatever" || []
        "%\\_"     || [THIRD_GAME_ID]
    }

    def "should filter games by user scores"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IntItemPropertiesDTO()
        fieldFilter.values = filterValues.stream().map { it.value }.toList()
        filter.userScore = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterValues                     || foundIds
        []                || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [ScoreGroup.NONE] || [FOURTH_GAME_ID]
        [ScoreGroup.O0X]                 || [SECOND_GAME_ID, THIRD_GAME_ID]
        [ScoreGroup.O1X]                 || []
        [ScoreGroup.O2X]                 || []
        [ScoreGroup.O3X]                 || []
        [ScoreGroup.O4X]                 || []
        [ScoreGroup.O5X]                 || []
        [ScoreGroup.O6X]                 || [FIRST_GAME_ID]
        [ScoreGroup.O7X]                 || []
        [ScoreGroup.O8X]                 || []
        [ScoreGroup.O9X]                 || []
        [ScoreGroup.O0X, ScoreGroup.O6X] || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
    }

    def "should filter games by community scores"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IntItemPropertiesDTO()
        fieldFilter.values = filterValues.stream().map { it.value }.toList()
        filter.communityScore = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterValues                                      || foundIds
        []                                                || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [ScoreGroup.NONE]                                 || [SECOND_GAME_ID, FOURTH_GAME_ID]
        [ScoreGroup.O0X]                                  || []
        [ScoreGroup.O1X]                                  || []
        [ScoreGroup.O2X]                                  || []
        [ScoreGroup.O3X]                                  || []
        [ScoreGroup.O4X]                                  || []
        [ScoreGroup.O5X]                                  || []
        [ScoreGroup.O6X]                                  || []
        [ScoreGroup.O7X]                                  || []
        [ScoreGroup.O8X]                                  || [FIRST_GAME_ID]
        [ScoreGroup.O9X]                                  || [THIRD_GAME_ID]
        [ScoreGroup.NONE, ScoreGroup.O8X, ScoreGroup.O9X] || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
    }

    def "should filter games by critic scores"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IntItemPropertiesDTO()
        fieldFilter.values = filterValues.stream().map { it.value }.toList()
        filter.criticScore = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterValues                                     || foundIds
        []                || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [ScoreGroup.NONE] || [FOURTH_GAME_ID]
        [ScoreGroup.O0X]                                 || []
        [ScoreGroup.O1X]                                 || [THIRD_GAME_ID]
        [ScoreGroup.O2X]                                 || []
        [ScoreGroup.O3X]                                 || [SECOND_GAME_ID]
        [ScoreGroup.O4X]                                 || []
        [ScoreGroup.O5X]                                 || []
        [ScoreGroup.O6X]                                 || []
        [ScoreGroup.O7X]                                 || []
        [ScoreGroup.O8X]                                 || []
        [ScoreGroup.O9X]                                 || [FIRST_GAME_ID]
        [ScoreGroup.O1X, ScoreGroup.O3X, ScoreGroup.O9X] || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID]
    }

    def "should filter games by last activity"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IntItemPropertiesDTO()
        fieldFilter.values = filterValues.stream().map { it.value }.toList()
        filter.lastActivity = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterValues                                            || foundIds
        []                      || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [PastTimeSegment.TODAY]                                 || [FIRST_GAME_ID]
        [PastTimeSegment.YESTERDAY]                             || []
        [PastTimeSegment.PAST_WEEK]                             || [SECOND_GAME_ID]
        [PastTimeSegment.PAST_MONTH]                            || []
        [PastTimeSegment.PAST_YEAR]                             || []
        [PastTimeSegment.MORE_THAN_YEAR]                        || [THIRD_GAME_ID]
        [PastTimeSegment.NEVER] || [FOURTH_GAME_ID]
        [PastTimeSegment.TODAY, PastTimeSegment.MORE_THAN_YEAR] || [FIRST_GAME_ID, THIRD_GAME_ID]
    }

    def "should filter games by recent activity"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IntItemPropertiesDTO()
        fieldFilter.values = filterValues.stream().map { it.value }.toList()
        filter.recentActivity = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterValues                                       || foundIds
        []                      || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [PastTimeSegment.TODAY]                            || [FIRST_GAME_ID]
        [PastTimeSegment.YESTERDAY]                        || []
        [PastTimeSegment.PAST_WEEK]                        || [SECOND_GAME_ID]
        [PastTimeSegment.PAST_MONTH]                       || []
        [PastTimeSegment.PAST_YEAR]                        || [THIRD_GAME_ID]
        [PastTimeSegment.MORE_THAN_YEAR]                   || []
        [PastTimeSegment.NEVER] || [FOURTH_GAME_ID]
        [PastTimeSegment.TODAY, PastTimeSegment.PAST_YEAR] || [FIRST_GAME_ID, THIRD_GAME_ID]
    }

    def "should filter games by added date"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IntItemPropertiesDTO()
        fieldFilter.values = filterValues.stream().map { it.value }.toList()
        filter.added = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterValues                                            || foundIds
        []                      || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [PastTimeSegment.TODAY]                                 || []
        [PastTimeSegment.YESTERDAY]                             || [FIRST_GAME_ID]
        [PastTimeSegment.PAST_WEEK]                             || []
        [PastTimeSegment.PAST_MONTH]                            || [SECOND_GAME_ID]
        [PastTimeSegment.PAST_YEAR]                             || [THIRD_GAME_ID]
        [PastTimeSegment.MORE_THAN_YEAR]                        || []
        [PastTimeSegment.NEVER] || [FOURTH_GAME_ID]
        [PastTimeSegment.PAST_MONTH, PastTimeSegment.PAST_YEAR] || [SECOND_GAME_ID, THIRD_GAME_ID]
    }

    def "should filter games by modified date"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IntItemPropertiesDTO()
        fieldFilter.values = filterValues.stream().map { it.value }.toList()
        filter.modified = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterValues                                       || foundIds
        []                                                 || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [PastTimeSegment.TODAY]                            || [FIRST_GAME_ID]
        [PastTimeSegment.YESTERDAY]                        || []
        [PastTimeSegment.PAST_WEEK]                        || []
        [PastTimeSegment.PAST_MONTH]                       || []
        [PastTimeSegment.PAST_YEAR]                        || [THIRD_GAME_ID]
        [PastTimeSegment.MORE_THAN_YEAR]                   || []
        [PastTimeSegment.NEVER]                            || [SECOND_GAME_ID, FOURTH_GAME_ID]
        [PastTimeSegment.PAST_YEAR, PastTimeSegment.NEVER] || [SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
    }

    def "should filter games by play time"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IntItemPropertiesDTO()
        fieldFilter.values = filterValues.stream().map { it.value }.toList()
        filter.playTime = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterValues                                                  || foundIds
        []                            || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [PlaytimeCategory.NOT_PLAYED] || [SECOND_GAME_ID, FOURTH_GAME_ID]
        [PlaytimeCategory.LESS_THAN_HOUR]                             || [FIRST_GAME_ID]
        [PlaytimeCategory.O1_10]                                      || []
        [PlaytimeCategory.O10_100]                                    || []
        [PlaytimeCategory.O100_500]                                   || []
        [PlaytimeCategory.O500_1000]                                  || [THIRD_GAME_ID]
        [PlaytimeCategory.O1000_PLUS]                                 || []
        [PlaytimeCategory.LESS_THAN_HOUR, PlaytimeCategory.O500_1000] || [FIRST_GAME_ID, THIRD_GAME_ID]
    }

    def "should filter games by install size"() {
        given:
        def filter = new GameListFilterDTO()
        def fieldFilter = new IntItemPropertiesDTO()
        fieldFilter.values = filterValues.stream().map { it.value }.toList()
        filter.installSize = fieldFilter

        when:
        def response = makeFilterRequest(filter)

        then:
        assertResults(response, foundIds)

        where:
        filterValues                                                   || foundIds
        []                      || [FIRST_GAME_ID, SECOND_GAME_ID, THIRD_GAME_ID, FOURTH_GAME_ID]
        [InstallSizeGroup.NONE] || [THIRD_GAME_ID, FOURTH_GAME_ID]
        [InstallSizeGroup.S0_0MB_100MB]                                || []
        [InstallSizeGroup.S1_100MB_1GB]                                || [FIRST_GAME_ID]
        [InstallSizeGroup.S2_1GB_5GB]                                  || []
        [InstallSizeGroup.S3_5GB_10GB]                                 || []
        [InstallSizeGroup.S4_10GB_20GB]                                || []
        [InstallSizeGroup.S5_20GB_40GB]                                || [SECOND_GAME_ID]
        [InstallSizeGroup.S6_40GB_100GB]                               || []
        [InstallSizeGroup.S7_100GB_PLUS]                               || []
        [InstallSizeGroup.S5_20GB_40GB, InstallSizeGroup.S1_100MB_1GB] || [FIRST_GAME_ID, SECOND_GAME_ID]
    }

    def assertResults(WebTestClient.ResponseSpec response, List<String> foundIds) {
        def entities = gameRepository.findByPlayniteIdIn(foundIds).collectList().block()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, GameListResultDTO.class))
                .expectNextMatches { result ->
                    def results = result.ungroupedResults
                    assert results != null
                    assert results.size() == foundIds.size()
                    assert results.size() == entities.size()
                    def resultsCopy = new ArrayList<>(results)
                    for (final def entity in entities) {
                        def matching = resultsCopy.findAll { it.id == entity.id }
                        assert (matching?.size() ?: 0) == 1
                        resultsCopy.removeAll(matching)
                    }
                    assert resultsCopy.size() == 0
                    true
                }
                .verifyComplete()
    }

    private GameDTO makeSaveRequest(Object dto, String uri, List<MultipartFile> files) {
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
        pluginWebTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri}/save")
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(GameDTO)
                .returnResult()
                .getResponseBody()
    }

    protected WebTestClient.ResponseSpec makeFilterRequest(GameListFilterDTO dto) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/panel/game/filter")
                        .build())
                .bodyValue(dto)
                .exchange()
    }
}
