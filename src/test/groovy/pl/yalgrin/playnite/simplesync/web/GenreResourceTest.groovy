package pl.yalgrin.playnite.simplesync.web


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.domain.Genre
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.dto.GenreDTO
import pl.yalgrin.playnite.simplesync.enums.ObjectType
import pl.yalgrin.playnite.simplesync.repository.GenreRepository
import pl.yalgrin.playnite.simplesync.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong

class GenreResourceTest extends AbstractObjectTest<Genre, GenreDTO> {

    @Autowired
    private GenreRepository genreRepository

    def "save single genre"() {
        given:
        GenreDTO dto = GenreDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("test")
                .build()

        when:
        def response = makeSaveRequest(dto)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, GenreDTO.class))
                .expectNextMatches { objectMatches(it, dto) }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto)
    }

    def "save multiple genres"() {
        given:
        List<GenreDTO> list = new ArrayList<>()
        for (int i = 0; i < 1000; i++) {
            list.add(GenreDTO.builder()
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
            StepVerifier.create(IntegrationTestUtil.getReturnMono(tuple.getV1(), GenreDTO.class))
                    .expectNextMatches { objectMatches(it, list.get(tuple.getV2())) }
                    .verifyComplete()
            true
        }

        and:
        list.stream().allMatch { dto -> assertEntityAndGetResponse(dto) }
    }

    def "save genre and then delete it"() {
        given:
        GenreDTO dto = GenreDTO.builder()
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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, GenreDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save and then remove repeatedly"() {
        given:
        GenreDTO dto = GenreDTO.builder()
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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, GenreDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save, modify and delete and await the change stream"() {
        given:
        GenreDTO toSave = GenreDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("new")
                .build()
        GenreDTO modified = toSave.toBuilder().name("some other name").build()
        GenreDTO removed = modified.toBuilder().removed(true).build()

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
                    assert change.getType() == ObjectType.Genre
                    assert change.getClientId() == "test"
                    assert change.getObjectId() != null
                    assert !change.isForceFetch()
                    newObjectId.set(change.getObjectId())
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GenreDTO.class))
                            .expectNextMatches { objectMatches(it, toSave) }
                            .verifyComplete()
                }
                .then {
                    makeSaveRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.Genre
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GenreDTO.class))
                            .expectNextMatches { objectMatches(it, modified) }
                            .verifyComplete()
                }
                .then {
                    makeDeleteRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.Genre
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GenreDTO.class))
                            .expectNextMatches { objectMatches(it, removed) }
                            .verifyComplete()
                }
                .thenCancel()
                .verify()
    }

    @Override
    protected String uri() {
        return "/api/genre"
    }

    @Override
    protected ObjectRepository<Genre> repository() {
        return genreRepository
    }

    @Override
    protected Class<? extends GenreDTO> dtoClass() {
        return GenreDTO.class
    }

    @Override
    boolean objectMatches(GenreDTO resultDTO, GenreDTO expectedDTO) {
        assert resultDTO.getId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }

    @Override
    boolean objectMatches(Genre resultDTO, GenreDTO expectedDTO) {
        assert resultDTO.getPlayniteId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }
}
