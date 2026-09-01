package pl.yalgrin.playnite.simplesync.web.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.client.dto.*
import pl.yalgrin.playnite.simplesync.client.enums.CheckResult
import pl.yalgrin.playnite.simplesync.client.repository.RegisteredClientRepository
import pl.yalgrin.playnite.simplesync.common.config.ConstantsKt
import pl.yalgrin.playnite.simplesync.dto.ErrorDTO
import pl.yalgrin.playnite.simplesync.security.SessionManager
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import reactor.test.StepVerifier

import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicReference

class ClientResourceTest extends SpockIntegrationTest {

    @Autowired
    private RegisteredClientRepository repository

    @Autowired
    private SessionManager sessionManager

    def "register a client"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("client name", ConstantsKt.CURRENT_API_VERSION)
        AtomicReference<RegisteredClientDTO> receivedInfo = new AtomicReference<>()

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, RegisteredClientDTO.class))
                .expectNextMatches { clientInfo ->
                    assert clientInfo.clientId != null
                    assert clientInfo.displayName == infoDTO.displayName
                    assert clientInfo.clientToken != null
                    receivedInfo.set(clientInfo)
                    true
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(receivedInfo.get().clientId, receivedInfo.get().clientToken, infoDTO.displayName)
    }

    def "register a client with older client version"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("client name", ConstantsKt.CURRENT_API_VERSION - 1)

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is4xxClientError()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, ErrorDTO.class))
                .expectNextMatches { error ->
                    assert error.message == "ApiVersionException.OUTDATED_CLIENT"
                    true
                }
                .verifyComplete()
    }

    def "register a client with newer client version"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("client name", ConstantsKt.CURRENT_API_VERSION + 1)

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is4xxClientError()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, ErrorDTO.class))
                .expectNextMatches { error ->
                    assert error.message == "ApiVersionException.OUTDATED_SERVER"
                    true
                }
                .verifyComplete()
    }

    def "register a client with no name"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("", ConstantsKt.CURRENT_API_VERSION)

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is4xxClientError()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, ErrorDTO.class))
                .expectNextMatches { error ->
                    assert error != null
                    assert error.message == "ValidationException"
                    assert error.fieldErrors?.size() == 1
                    assert error.fieldErrors.first.message == "validation.notNull"
                    true
                }
                .verifyComplete()
    }

    def "register a client with too long name"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("1234567890123456789012345678901234567890123456789|1234567890123456789012345678901234567890123456789|1234567890123456789012345678901234567890123456789|1234567890123456789012345678901234567890123456789|a", ConstantsKt.CURRENT_API_VERSION)

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is4xxClientError()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, ErrorDTO.class))
                .expectNextMatches { error ->
                    assert error != null
                    assert error.message == "ValidationException"
                    assert error.fieldErrors?.size() == 1
                    assert error.fieldErrors.first.message == "validation.maxSize"
                    true
                }
                .verifyComplete()
    }

    def "register a client and then change the name"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("client name", ConstantsKt.CURRENT_API_VERSION)
        AtomicReference<RegisteredClientDTO> receivedInfo = new AtomicReference<>()

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, RegisteredClientDTO.class))
                .expectNextMatches { clientInfo ->
                    assert clientInfo.clientId != null
                    assert clientInfo.displayName == infoDTO.displayName
                    assert clientInfo.clientToken != null
                    receivedInfo.set(clientInfo)
                    true
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(receivedInfo.get().clientId, receivedInfo.get().clientToken, infoDTO.displayName)

        when:
        def makeChangeResponse = makeChangeNameRequest(receivedInfo.get(), "different name")

        then:
        makeChangeResponse.expectStatus().is2xxSuccessful()

        and:
        assertEntityAndGetResponse(receivedInfo.get().clientId, receivedInfo.get().clientToken, "different name")
    }

    def "register a client and then change the name to an empty one"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("client name", ConstantsKt.CURRENT_API_VERSION)
        AtomicReference<RegisteredClientDTO> receivedInfo = new AtomicReference<>()

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, RegisteredClientDTO.class))
                .expectNextMatches { clientInfo ->
                    assert clientInfo.clientId != null
                    assert clientInfo.displayName == infoDTO.displayName
                    assert clientInfo.clientToken != null
                    receivedInfo.set(clientInfo)
                    true
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(receivedInfo.get().clientId, receivedInfo.get().clientToken, infoDTO.displayName)

        when:
        def makeChangeResponse = makeChangeNameRequest(receivedInfo.get(), "")

        then:
        makeChangeResponse.expectStatus().is4xxClientError()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(makeChangeResponse, ErrorDTO.class))
                .expectNextMatches { error ->
                    assert error != null
                    assert error.message == "ValidationException"
                    assert error.fieldErrors?.size() == 1
                    assert error.fieldErrors.first.message == "validation.notNull"
                    true
                }
                .verifyComplete()
    }

    def "register a client and then change the name to a one that is too long"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("client name", ConstantsKt.CURRENT_API_VERSION)
        AtomicReference<RegisteredClientDTO> receivedInfo = new AtomicReference<>()

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, RegisteredClientDTO.class))
                .expectNextMatches { clientInfo ->
                    assert clientInfo.clientId != null
                    assert clientInfo.displayName == infoDTO.displayName
                    assert clientInfo.clientToken != null
                    receivedInfo.set(clientInfo)
                    true
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(receivedInfo.get().clientId, receivedInfo.get().clientToken, infoDTO.displayName)

        when:
        def makeChangeResponse = makeChangeNameRequest(receivedInfo.get(), "1234567890123456789012345678901234567890123456789|1234567890123456789012345678901234567890123456789|1234567890123456789012345678901234567890123456789|1234567890123456789012345678901234567890123456789|a")

        then:
        makeChangeResponse.expectStatus().is4xxClientError()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(makeChangeResponse, ErrorDTO.class))
                .expectNextMatches { error ->
                    assert error != null
                    assert error.message == "ValidationException"
                    assert error.fieldErrors?.size() == 1
                    assert error.fieldErrors.first.message == "validation.maxSize"
                    true
                }
                .verifyComplete()
    }

    def "register a client then test the connection - result ok, no session"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("client name", ConstantsKt.CURRENT_API_VERSION)
        AtomicReference<RegisteredClientDTO> receivedInfo = new AtomicReference<>()

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, RegisteredClientDTO.class))
                .expectNextMatches { clientInfo ->
                    assert clientInfo.clientId != null
                    assert clientInfo.displayName == infoDTO.displayName
                    assert clientInfo.clientToken != null
                    receivedInfo.set(clientInfo)
                    true
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(receivedInfo.get().clientId, receivedInfo.get().clientToken, infoDTO.displayName)

        when:
        def checkResponse = makeCheckRequest(ConstantsKt.CURRENT_API_VERSION, receivedInfo.get())

        then:
        checkResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(checkResponse, CheckResultDTO.class))
                .expectNextMatches { res ->
                    assert res != null
                    assert res.result == CheckResult.OK
                    assert res.registrationSpecified
                    assert res.registrationValid
                    assert res.displayClientName == infoDTO.displayName
                    assert !res.sessionActive
                    true
                }
                .verifyComplete()
    }

    def "register a client then test the connection - result ok, session exists"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("client name", ConstantsKt.CURRENT_API_VERSION)
        AtomicReference<RegisteredClientDTO> receivedInfo = new AtomicReference<>()

        when:
        def response = makeRegisterRequest(infoDTO)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, RegisteredClientDTO.class))
                .expectNextMatches { clientInfo ->
                    assert clientInfo.clientId != null
                    assert clientInfo.displayName == infoDTO.displayName
                    assert clientInfo.clientToken != null
                    receivedInfo.set(clientInfo)
                    true
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(receivedInfo.get().clientId, receivedInfo.get().clientToken, infoDTO.displayName)


        when:
        def sessionInfo = new SessionInfoDTO(receivedInfo.get().clientId, receivedInfo.get().displayName, UUID.randomUUID().toString())
        sessionManager.saveSessionInfo(sessionInfo)
        def checkResponse = makeCheckRequest(ConstantsKt.CURRENT_API_VERSION, receivedInfo.get(), sessionInfo.sessionId)

        then:
        checkResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(checkResponse, CheckResultDTO.class))
                .expectNextMatches { res ->
                    assert res != null
                    assert res.result == CheckResult.OK
                    assert res.registrationSpecified
                    assert res.registrationValid
                    assert res.displayClientName == infoDTO.displayName
                    assert res.sessionActive
                    true
                }
                .verifyComplete()

        cleanup:
        sessionManager.removeSessionInfo(sessionInfo)
    }

    def "test the connection with no credentials"() {
        when:
        def checkResponse = makeCheckRequest(ConstantsKt.CURRENT_API_VERSION, null)

        then:
        checkResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(checkResponse, CheckResultDTO.class))
                .expectNextMatches { res ->
                    assert res != null
                    assert res.result == CheckResult.OK
                    assert !res.registrationSpecified
                    assert !res.registrationValid
                    assert res.displayClientName.isBlank()
                    assert !res.sessionActive
                    true
                }
                .verifyComplete()
    }

    def "test the connection on totally invalid client"() {
        when:
        def checkResponse = makeCheckRequest(ConstantsKt.CURRENT_API_VERSION, new RegisteredClientDTO("i", "don't", "exist"))

        then:
        checkResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(checkResponse, CheckResultDTO.class))
                .expectNextMatches { res ->
                    assert res != null
                    assert res.result == CheckResult.OK
                    assert res.registrationSpecified
                    assert !res.registrationValid
                    assert res.displayClientName.isBlank()
                    assert !res.sessionActive
                    true
                }
                .verifyComplete()
    }

    protected WebTestClient.ResponseSpec makeRegisterRequest(RegistrationRequestDTO dto) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/client/register")
                        .build())
                .bodyValue(dto)
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeChangeNameRequest(RegisteredClientDTO dto, String newName) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/client/change-name")
                        .queryParam("newName", newName)
                        .build())
                .header("X-Client-Id", dto.clientId)
                .header("X-Client-Token", dto.clientToken)
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeCheckRequest(Integer apiVersion, RegisteredClientDTO dto, String sessionId = "") {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/client/check")
                        .build())
                .bodyValue(new CheckRequestDTO(apiVersion))
                .header("X-Client-Id", dto?.clientId ?: "")
                .header("X-Client-Token", dto?.clientToken ?: "")
                .header("X-Session-Id", sessionId)
                .exchange()
    }

    protected assertEntityAndGetResponse(String clientId, String token, String displayName) {
        def savedEntity = repository.findById(clientId).block()
        assert savedEntity != null
        assert savedEntity.clientId == clientId
        assert savedEntity.displayName == displayName
        assert savedEntity.clientToken == sha1(token)
        true
    }

    String sha1(String input) {
        def digest = MessageDigest.getInstance("SHA-1")
        digest.update(input.getBytes())
        return digest.digest().collect { String.format("%02x", it) }.join("")
    }
}
