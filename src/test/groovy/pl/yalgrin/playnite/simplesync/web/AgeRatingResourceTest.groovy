package pl.yalgrin.playnite.simplesync.web


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.domain.AgeRating
import pl.yalgrin.playnite.simplesync.dto.AgeRatingDTO
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.enums.ObjectType
import pl.yalgrin.playnite.simplesync.repository.AgeRatingRepository
import pl.yalgrin.playnite.simplesync.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong

class AgeRatingResourceTest extends AbstractObjectTest<AgeRating, AgeRatingDTO> {

    @Autowired
    private AgeRatingRepository ageRatingRepository

    def "save single age rating"() {
        given:
        AgeRatingDTO dto = AgeRatingDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("test")
                .build()

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
            list.add(AgeRatingDTO.builder()
                    .id("id-" + i)
                    .name("name " + i)
                    .build())
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
        AgeRatingDTO dto = AgeRatingDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("to-delete")
                .build()

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
        AgeRatingDTO dto = AgeRatingDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("to-delete-2")
                .build()

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
        AgeRatingDTO toSave = AgeRatingDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("new")
                .build()
        AgeRatingDTO modified = toSave.toBuilder().name("some other name").build()
        AgeRatingDTO removed = modified.toBuilder().removed(true).build()

        when:
        def changeRequest = makeChangeStreamRequest()
        def responseFlux = changeRequest.returnResult(ChangeDTO.class).responseBody

        then:
        AtomicLong newObjectId = new AtomicLong(-1)
        StepVerifier.create(responseFlux)
                .expectSubscription()
                .then {
                    makeSaveRequest(toSave).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.AgeRating
                    assert change.getClientId() == "test"
                    assert change.getObjectId() != null
                    assert !change.isForceFetch()
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
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.AgeRating
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.isForceFetch()
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
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.AgeRating
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.isForceFetch()
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
        assert resultDTO.getId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }

    @Override
    boolean objectMatches(AgeRating resultDTO, AgeRatingDTO expectedDTO) {
        assert resultDTO.getPlayniteId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }
}
