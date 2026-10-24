package pl.yalgrin.playnite.simplesync.web.panel

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.client.dto.RegisteredClientDTO
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationRequestDTO
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.dto.SessionSettingsDTO
import pl.yalgrin.playnite.simplesync.client.service.RegisteredClientService
import pl.yalgrin.playnite.simplesync.common.config.ConstantsKt
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import pl.yalgrin.playnite.simplesync.library.dto.PanelObjectDTO
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.library.service.MetadataService
import pl.yalgrin.playnite.simplesync.panel.dto.CsrfResponse
import pl.yalgrin.playnite.simplesync.panel.dto.LoginRequest
import pl.yalgrin.playnite.simplesync.panel.repository.PanelUserRepository
import pl.yalgrin.playnite.simplesync.panel.service.PanelUserService
import pl.yalgrin.playnite.simplesync.security.SessionManager
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import reactor.test.StepVerifier
import spock.lang.Shared
import tools.jackson.databind.json.JsonMapper

abstract class AbstractPanelObjectTest<E extends LibraryObjectEntity, D extends PanelObjectDTO> extends SpockIntegrationTest {
    @Shared
    @Autowired
    private ConnectionFactory connectionFactory
    @Shared
    @Autowired
    private RegisteredClientService registeredClientService
    @Shared
    @Autowired
    private PanelUserService userService
    @Shared
    @Autowired
    private PanelUserRepository userRepository
    @Shared
    @Autowired
    private SessionManager sessionManager
    @Shared
    @Autowired
    private MetadataService metadataService
    @Shared
    @Autowired
    protected JsonMapper jsonMapper
    @Shared
    protected WebTestClient rawWebTestClient

    @Shared
    protected String clientId
    @Shared
    protected RegisteredClientDTO otherClientInfo

    def setupSpec() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-db.sql"))
        populator.populate(connectionFactory).block()

        userService.init().block()

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
        clientId = user.id.toString()

        def sessionId = UUID.randomUUID().toString()

        def sessionInfo = new SessionInfoDTO(clientId, user.username, sessionId, true)
        sessionManager.saveSessionInfo(sessionInfo)
        sessionManager.updateSettings(sessionInfo, new SessionSettingsDTO(true))

        otherClientInfo = registeredClientService.register(new RegistrationRequestDTO("other-client", ConstantsKt.CURRENT_API_VERSION)).block()

        rawWebTestClient = webTestClient
        webTestClient = webTestClient.mutate()
                .defaultHeader("X-Session-Id", sessionId)
                .build()
    }

    def setup() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-data.sql"))
        populator.populate(connectionFactory).block()

        metadataService.init()
    }

    protected WebTestClient.ResponseSpec makeGetRequest(Long id) {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri()}/$id")
                        .build())
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeSaveRequest(D dto) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri()}/save")
                        .build())
                .bodyValue(dto)
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeDeleteRequest(D dto) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri()}/delete")
                        .build())
                .bodyValue(dto)
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeChangeStreamRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/change/stream")
                        .build())
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeConnectRequest(RegisteredClientDTO clientDTO) {
        rawWebTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/client/connect")
                        .build())
                .header("X-Client-Id", clientDTO.clientId)
                .header("X-Client-Token", clientDTO.clientToken)
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeEnableChangeStreamRequest(RegisteredClientDTO clientDTO, String sessionId) {
        rawWebTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/client/enable-change-stream")
                        .build())
                .header("X-Client-Id", clientDTO.clientId)
                .header("X-Client-Token", clientDTO.clientToken)
                .header("X-Session-Id", sessionId)
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeDisableChangeStreamRequest(RegisteredClientDTO clientDTO, String sessionId) {
        rawWebTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/client/disable-change-stream")
                        .build())
                .header("X-Client-Id", clientDTO.clientId)
                .header("X-Client-Token", clientDTO.clientToken)
                .header("X-Session-Id", sessionId)
                .exchange()
    }

    protected assertEntityAndGetResponse(D dto, Long id) {
        def savedEntity = repository().findById(id).block()
        assert savedEntity != null

        assert objectMatches(savedEntity, dto)

        def getResponse = makeGetRequest(savedEntity.getId())

        getResponse.expectStatus().is2xxSuccessful()

        StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, dtoClass()))
                .expectNextMatches {
                    objectMatches(it, dto)
                }
                .verifyComplete()
        true
    }

    boolean assertDeleted(Long id) {
        def savedEntity = repository().findById(id).block()
        if (savedEntity == null) {
            true
        } else {
            assert savedEntity.isRemoved()

            def getResponse = makeGetRequest(savedEntity.getId())

            getResponse.expectStatus().is4xxClientError()

            StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, dtoClass()))
                    .verifyComplete()
            true
        }
    }

    protected abstract String uri();

    protected abstract ObjectRepository<E> repository();

    protected abstract Class<? extends D> dtoClass();

    protected abstract boolean objectMatches(D resultDTO, D expectedDTO)

    protected abstract boolean objectMatches(E resultDTO, D expectedDTO)
}
