package pl.yalgrin.playnite.simplesync.web.library

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.client.enums.MessageType
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.client.message.InitializationMessage
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.FilterPreset
import pl.yalgrin.playnite.simplesync.library.dto.FilterPresetDTO
import pl.yalgrin.playnite.simplesync.library.repository.FilterPresetRepository
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.JsonMapperUtil
import pl.yalgrin.playnite.simplesync.util.library.FilterPresetAssertionUtil
import pl.yalgrin.playnite.simplesync.util.library.FilterPresetFactoryUtil
import reactor.test.StepVerifier
import tools.jackson.databind.ObjectMapper

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class FilterPresetResourceTest extends AbstractObjectTest<FilterPreset, FilterPresetDTO> {

    @Autowired
    private FilterPresetRepository filterPresetRepository
    @Autowired
    private ObjectMapper objectMapper

    def "save single filter preset"() {
        given:
        FilterPresetDTO dto = FilterPresetFactoryUtil.createFilterPreset(UUID.randomUUID().toString(), "test")
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def response = makeSaveRequest(dto)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, FilterPresetDTO.class))
                .expectNextMatches {
                    assert it.serverId != null
                    assert it.createdAt != null
                    assert it.createdBy != null
                    assert it.modifiedAt != null
                    assert it.modifiedBy != null
                    newObjectId.set(it.serverId)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, newObjectId.get())
    }

    def "save multiple filter presets"() {
        given:
        List<FilterPresetDTO> list = new ArrayList<>()
        for (int i = 0; i < 200; i++) {
            list.add(FilterPresetFactoryUtil.filterPresetWithIndex(i))
        }
        List<Long> createdIds = new ArrayList<>()

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
                    .expectNextMatches {
                        assert it.serverId != null
                        assert it.createdAt != null
                        assert it.createdBy != null
                        assert it.modifiedAt != null
                        assert it.modifiedBy != null
                        createdIds.add(it.serverId)
                        objectMatches(it, list.get(tuple.getV2()))
                    }
                    .verifyComplete()
            true
        }

        and:
        list.withIndex().stream().allMatch { tuple -> assertEntityAndGetResponse(tuple.getV1(), createdIds.get(tuple.getV2())) }
    }

    def "save filter preset and then delete it"() {
        given:
        FilterPresetDTO dto = FilterPresetFactoryUtil.randomFilterPreset()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, FilterPresetDTO.class))
                .expectNextMatches {
                    assert it.serverId != null
                    assert it.createdAt != null
                    assert it.createdBy != null
                    assert it.modifiedAt != null
                    assert it.modifiedBy != null
                    newObjectId.set(it.serverId)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, newObjectId.get())

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
        FilterPresetDTO dto = FilterPresetFactoryUtil.randomFilterPreset()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, FilterPresetDTO.class))
                .expectNextMatches {
                    assert it.serverId != null
                    assert it.createdAt != null
                    assert it.createdBy != null
                    assert it.modifiedAt != null
                    assert it.modifiedBy != null
                    newObjectId.set(it.serverId)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, newObjectId.get())

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
        FilterPresetDTO toSave = FilterPresetFactoryUtil.randomFilterPreset()
        FilterPresetDTO modified = toSave.withName("some other name")
        FilterPresetDTO removed = modified.withRemoved(true)

        when:
        def changeRequest = makeConnectRequest(otherClientInfo)
        def responseFlux = changeRequest.returnResult(new ParameterizedTypeReference<String>() {}).responseBody

        then:
        List<Long> collectedIds = Collections.synchronizedList(new ArrayList<>());
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
                    def result = makeSaveRequest(toSave).expectStatus().is2xxSuccessful()
                            .expectBody(FilterPresetDTO.class)
                            .returnResult()
                            .responseBody
                    FilterPresetAssertionUtil.assertFilterPreset(toSave, result)
                    assert result.serverId != null
                    assert result.createdAt != null
                    assert result.createdBy != null
                    assert result.modifiedAt != null
                    assert result.modifiedBy != null
                    collectedIds.add(result.serverId)
                    if (collectedIds.size() > 1) {
                        assert collectedIds.stream().distinct().size() == 1
                    }
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.FILTER_PRESET
                    assert change.getClientId() == clientId
                    assert change.getObjectId() != null
                    assert change.getDiffParentObjectId() == null
                    assert !change.isForceFetch()
                    collectedIds.add(change.getObjectId())
                    if (collectedIds.size() > 1) {
                        assert collectedIds.stream().distinct().size() == 1
                    }
                    true
                }
                .then {
                    def getResponse = makeGetRequest(collectedIds.first)

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, FilterPresetDTO.class))
                            .expectNextMatches {
                                assert it.serverId == collectedIds.first
                                assert it.createdAt != null
                                assert it.createdBy != null
                                assert it.modifiedAt != null
                                assert it.modifiedBy != null
                                objectMatches(it, toSave)
                            }
                            .verifyComplete()
                }
                .then {
                    def result = makeSaveRequest(modified).expectStatus().is2xxSuccessful()
                            .expectBody(FilterPresetDTO.class)
                            .returnResult()
                            .responseBody
                    FilterPresetAssertionUtil.assertFilterPreset(modified, result)
                    assert result.serverId == collectedIds.first
                    assert result.createdAt != null
                    assert result.createdBy != null
                    assert result.modifiedAt != null
                    assert result.modifiedBy != null
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.FILTER_PRESET
                    assert change.getClientId() == clientId
                    assert change.getObjectId() == collectedIds.first
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(collectedIds.first)

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, FilterPresetDTO.class))
                            .expectNextMatches {
                                assert it.serverId == collectedIds.first
                                assert it.createdAt != null
                                assert it.createdBy != null
                                assert it.modifiedAt != null
                                assert it.modifiedBy != null
                                objectMatches(it, modified)
                            }
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
                    assert change.getType() == ObjectType.FILTER_PRESET
                    assert change.getClientId() == clientId
                    assert change.getObjectId() == collectedIds.first
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(collectedIds.first)

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, FilterPresetDTO.class))
                            .expectNextMatches {
                                assert it.serverId == collectedIds.first
                                assert it.createdAt != null
                                assert it.createdBy != null
                                assert it.modifiedAt != null
                                assert it.modifiedBy != null
                                objectMatches(it, removed)
                            }
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
        FilterPresetAssertionUtil.assertFilterPreset(expectedDTO, resultDTO)
    }

    @Override
    boolean objectMatches(FilterPreset resultEntity, FilterPresetDTO expectedDTO) {
        FilterPresetAssertionUtil.assertFilterPresetEntity(expectedDTO, resultEntity)
    }
}
