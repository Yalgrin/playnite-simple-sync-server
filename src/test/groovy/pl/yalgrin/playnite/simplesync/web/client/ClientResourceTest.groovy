package pl.yalgrin.playnite.simplesync.web.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.client.dto.RegisteredClientDTO
import pl.yalgrin.playnite.simplesync.client.dto.RegistrationRequestDTO
import pl.yalgrin.playnite.simplesync.client.repository.RegisteredClientRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import reactor.test.StepVerifier

import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicReference

class ClientResourceTest extends SpockIntegrationTest {

    @Autowired
    private RegisteredClientRepository repository

    def "register a client"() {
        given:
        RegistrationRequestDTO infoDTO = new RegistrationRequestDTO("client name")
        AtomicReference<RegisteredClientDTO> receivedInfo = new AtomicReference<>()

        when:
        def response = makeSaveRequest(infoDTO)

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
        assertEntityAndGetResponse(receivedInfo.get().clientId, receivedInfo.get().clientToken)
    }

    protected WebTestClient.ResponseSpec makeSaveRequest(RegistrationRequestDTO dto) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/client/register")
                        .build())
                .bodyValue(dto)
                .exchange()
    }

    protected assertEntityAndGetResponse(String clientId, String token) {
        def savedEntity = repository.findById(clientId).block()
        assert savedEntity != null
        assert savedEntity.clientId == clientId
        assert savedEntity.displayName == "client name"
        assert savedEntity.clientToken == sha1(token)
        true
    }

    String sha1(String input) {
        def digest = MessageDigest.getInstance("SHA-1")
        digest.update(input.getBytes())
        return digest.digest().collect { String.format("%02x", it) }.join("")
    }
}
