package pl.yalgrin.playnite.simplesync.web


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.domain.FilterPreset
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.dto.FilterPresetDTO
import pl.yalgrin.playnite.simplesync.enums.ObjectType
import pl.yalgrin.playnite.simplesync.repository.FilterPresetRepository
import pl.yalgrin.playnite.simplesync.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong

class FilterPresetResourceTest extends AbstractObjectTest<FilterPreset, FilterPresetDTO> {

    @Autowired
    private FilterPresetRepository filterPresetRepository

    def "save single filter preset"() {
        given:
        FilterPresetDTO dto = FilterPresetDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("test")
                .build()

        when:
        def response = makeSaveRequest(dto)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, FilterPresetDTO.class))
                .expectNextMatches { objectMatches(it, dto) }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto)
    }

    def "save multiple filter presets"() {
        given:
        List<FilterPresetDTO> list = new ArrayList<>()
        for (int i = 0; i < 1000; i++) {
            list.add(FilterPresetDTO.builder()
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
            StepVerifier.create(IntegrationTestUtil.getReturnMono(tuple.getV1(), FilterPresetDTO.class))
                    .expectNextMatches { objectMatches(it, list.get(tuple.getV2())) }
                    .verifyComplete()
            true
        }

        and:
        list.stream().allMatch { dto -> assertEntityAndGetResponse(dto) }
    }

    def "save filterPreset and then delete it"() {
        given:
        FilterPresetDTO dto = FilterPresetDTO.builder()
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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, FilterPresetDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save and then remove repeatedly"() {
        given:
        FilterPresetDTO dto = FilterPresetDTO.builder()
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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, FilterPresetDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save, modify and delete and await the change stream"() {
        given:
        FilterPresetDTO toSave = FilterPresetDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("new")
                .build()
        FilterPresetDTO modified = toSave.toBuilder().name("some other name").build()
        FilterPresetDTO removed = modified.toBuilder().removed(true).build()

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
                    assert change.getType() == ObjectType.FilterPreset
                    assert change.getClientId() == "test"
                    assert change.getObjectId() != null
                    assert !change.isForceFetch()
                    newObjectId.set(change.getObjectId())
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, FilterPresetDTO.class))
                            .expectNextMatches { objectMatches(it, toSave) }
                            .verifyComplete()
                }
                .then {
                    makeSaveRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.FilterPreset
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, FilterPresetDTO.class))
                            .expectNextMatches { objectMatches(it, modified) }
                            .verifyComplete()
                }
                .then {
                    makeDeleteRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.FilterPreset
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, FilterPresetDTO.class))
                            .expectNextMatches { objectMatches(it, removed) }
                            .verifyComplete()
                }
                .thenCancel()
                .verify()
    }

    @Override
    protected String uri() {
        return "/api/filter-preset"
    }

    @Override
    protected ObjectRepository<FilterPreset> repository() {
        return filterPresetRepository
    }

    @Override
    protected Class<? extends FilterPresetDTO> dtoClass() {
        return FilterPresetDTO.class
    }

    @Override
    boolean objectMatches(FilterPresetDTO resultDTO, FilterPresetDTO expectedDTO) {
        assert resultDTO.getId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }

    @Override
    boolean objectMatches(FilterPreset resultDTO, FilterPresetDTO expectedDTO) {
        assert resultDTO.getPlayniteId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }
}
