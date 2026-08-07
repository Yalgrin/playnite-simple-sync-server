package pl.yalgrin.playnite.simplesync.web.objects

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.client.dto.RegisteredClientDTO
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationRequestDTO
import pl.yalgrin.playnite.simplesync.client.dto.SessionInfoDTO
import pl.yalgrin.playnite.simplesync.client.service.RegisteredClientService
import pl.yalgrin.playnite.simplesync.dto.objects.AbstractObjectDTO
import pl.yalgrin.playnite.simplesync.library.domain.LibraryObjectEntity
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.security.SessionManager
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import reactor.test.StepVerifier
import tools.jackson.databind.json.JsonMapper

abstract class AbstractObjectTest<E extends LibraryObjectEntity, D extends AbstractObjectDTO> extends SpockIntegrationTest {
    @Autowired
    private ConnectionFactory connectionFactory
    @Autowired
    private RegisteredClientService registeredClientService
    @Autowired
    private SessionManager sessionManager
    @Autowired
    protected JsonMapper jsonMapper
    protected WebTestClient rawWebTestClient

    protected String clientId
    protected RegisteredClientDTO otherClientInfo

    def setup() {
        def populator = new ResourceDatabasePopulator()
        populator.addScript(new ClassPathResource("/sql/clear-data.sql"))
        populator.populate(connectionFactory).block()

        def clientInfo = registeredClientService.register(new RegistrationRequestDTO("test-user", pl.yalgrin.playnite.simplesync.common.config.ConstantsKt.CURRENT_API_VERSION)).block()
        def sessionId = UUID.randomUUID().toString()
        sessionManager.saveSessionInfo(new SessionInfoDTO(clientInfo.clientId, clientInfo.displayName, sessionId))
        otherClientInfo = registeredClientService.register(new RegistrationRequestDTO("other-client", pl.yalgrin.playnite.simplesync.common.config.ConstantsKt.CURRENT_API_VERSION)).block()

        rawWebTestClient = webTestClient
        webTestClient = webTestClient.mutate()
                .defaultHeader("X-Client-Id", clientInfo.clientId)
                .defaultHeader("X-Client-Token", clientInfo.clientToken)
                .defaultHeader("X-Session-Id", sessionId)
                .build()

        clientId = clientInfo.clientId
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

    protected assertEntityAndGetResponse(D dto) {
        def savedEntities = repository().findByPlayniteId(dto.id).collectList().block()
        assert savedEntities.size() == 1

        def savedEntity = savedEntities[0]
        assert objectMatches(savedEntity, dto)

        def getResponse = makeGetRequest(savedEntity.getId())

        getResponse.expectStatus().is2xxSuccessful()

        StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, dtoClass()))
                .expectNextMatches { objectMatches(it, dto) }
                .verifyComplete()
        true
    }

    boolean assertDeleted(D dto) {
        def savedEntities = repository().findByPlayniteId(dto.id).collectList().block()
        if (savedEntities.isEmpty()) {
            true
        } else {
            assert savedEntities.size() == 1

            def savedEntity = savedEntities[0]
            assert savedEntity.isRemoved()

            def getResponse = makeGetRequest(savedEntity.getId())

            getResponse.expectStatus().is2xxSuccessful()

            StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, dtoClass()))
                    .expectNextMatches {
                        assert it.isRemoved()
                        true
                    }
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
