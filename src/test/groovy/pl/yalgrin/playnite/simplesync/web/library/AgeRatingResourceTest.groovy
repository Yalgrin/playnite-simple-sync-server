package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.client.enums.MessageType
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.client.message.InitializationMessage
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.AgeRating
import pl.yalgrin.playnite.simplesync.library.dto.AgeRatingDTO
import pl.yalgrin.playnite.simplesync.library.repository.AgeRatingRepository
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.JsonMapperUtil
import pl.yalgrin.playnite.simplesync.util.library.AgeRatingAssertionUtil
import pl.yalgrin.playnite.simplesync.util.library.AgeRatingFactoryUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class AgeRatingResourceTest extends AbstractObjectTest<AgeRating, AgeRatingDTO> {

    @Autowired
    private AgeRatingRepository ageRatingRepository

    def "save single age rating"() {
        given:
        AgeRatingDTO dto = AgeRatingFactoryUtil.createAgeRating(UUID.randomUUID().toString(), "test")

        when:
        def response = makeSaveRequest(dto)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, AgeRatingDTO.class))
                .expectNextMatches { objectMatches(it, dto) }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto)
    }

    def "save multiple age ratings"() {
        given:
        List<AgeRatingDTO> list = new ArrayList<>()
        for (int i = 0; i < 1000; i++) {
            list.add(AgeRatingFactoryUtil.ageRatingWithIndex(i))
        }

        when:
        List<CompletableFuture<WebTestClient.ResponseSpec>> futures = list.stream()
                .map { dto -> CompletableFuture.supplyAsync({ makeSaveRequest(dto) }) }
                .toList()
        List<WebTestClient.ResponseSpec> responses = futures.stream()
                .map(CompletableFuture::join)
                .toList() as List<WebTestClient.ResponseSpec>

        then:
        responses.stream().allMatch { response ->
            response.expectStatus().is2xxSuccessful()
            true
        }

        and:
        responses.withIndex().stream().allMatch { tuple ->
            StepVerifier.create(IntegrationTestUtil.getReturnMono(tuple.getV1(), AgeRatingDTO.class))
                    .expectNextMatches { objectMatches(it, list.get(tuple.getV2())) }
                    .verifyComplete()
            true
        }

        and:
        list.stream().allMatch { dto -> assertEntityAndGetResponse(dto) }
    }

    def "save age rating and then delete it"() {
        given:
        AgeRatingDTO dto = AgeRatingFactoryUtil.randomAgeRating()

        when:
        def saveResponse = makeSaveRequest(dto)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        assertEntityAndGetResponse(dto)

        when:
        def deleteResponse = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, AgeRatingDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save and then remove repeatedly"() {
        given:
        AgeRatingDTO dto = AgeRatingFactoryUtil.randomAgeRating()

        when:
        def saveResponse = makeSaveRequest(dto)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        assertEntityAndGetResponse(dto)

        when:
        def deleteResponse = makeDeleteRequest(dto)
        def deleteResponse2 = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()
        deleteResponse2.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, AgeRatingDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save, modify and delete and await the change stream"() {
        given:
        AgeRatingDTO toSave = AgeRatingFactoryUtil.randomAgeRating()
        AgeRatingDTO modified = toSave.withName("some other name")
        AgeRatingDTO removed = modified.withRemoved(true)

        when:
        def changeRequest = makeConnectRequest(otherClientInfo)
        def responseFlux = changeRequest.returnResult(new ParameterizedTypeReference<String>() {}).responseBody

        then:
        AtomicLong newObjectId = new AtomicLong(-1)
        AtomicReference<String> sessionId = new AtomicReference<>()
        StepVerifier.create(responseFlux)
                .expectSubscription()
                .expectNextMatches { str ->
                    def message = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert message.messageType == MessageType.INITIALIZATION
                    assert message instanceof InitializationMessage
                    sessionId.set(message.sessionId)
                    true
                }
                .then {
                    makeEnableChangeStreamRequest(otherClientInfo, sessionId.get())
                }
                .then {
                    makeSaveRequest(toSave).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.AGE_RATING
                    assert change.getClientId() == clientId
                    assert change.getObjectId() != null
                    assert !change.getForceFetch()
                    newObjectId.set(change.getObjectId())
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, AgeRatingDTO.class))
                            .expectNextMatches { objectMatches(it, toSave) }
                            .verifyComplete()
                }
                .then {
                    makeSaveRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.AGE_RATING
                    assert change.getClientId() == clientId
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.getForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, AgeRatingDTO.class))
                            .expectNextMatches { objectMatches(it, modified) }
                            .verifyComplete()
                }
                .then {
                    makeDeleteRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.AGE_RATING
                    assert change.getClientId() == clientId
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.getForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, AgeRatingDTO.class))
                            .expectNextMatches { objectMatches(it, removed) }
                            .verifyComplete()
                }
                .thenCancel()
                .verify()
    }

    @Override
    protected String uri() {
        return "/api/age-rating"
    }

    @Override
    protected ObjectRepository<AgeRating> repository() {
        return ageRatingRepository
    }

    @Override
    protected Class<? extends AgeRatingDTO> dtoClass() {
        return AgeRatingDTO.class
    }

    @Override
    boolean objectMatches(AgeRatingDTO resultDTO, AgeRatingDTO expectedDTO) {
        AgeRatingAssertionUtil.assertAgeRating(expectedDTO, resultDTO)
    }

    @Override
    boolean objectMatches(AgeRating resultDTO, AgeRatingDTO expectedDTO) {
        AgeRatingAssertionUtil.assertAgeRatingEntity(expectedDTO, resultDTO)
    }
}
