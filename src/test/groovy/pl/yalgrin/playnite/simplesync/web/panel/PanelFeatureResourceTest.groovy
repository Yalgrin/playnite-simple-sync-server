package pl.yalgrin.playnite.simplesync.web.panel

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.client.enums.MessageType
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.client.message.InitializationMessage
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Feature
import pl.yalgrin.playnite.simplesync.library.repository.FeatureRepository
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.panel.dto.PanelFeatureDTO
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.JsonMapperUtil
import pl.yalgrin.playnite.simplesync.util.panel.PanelFeatureAssertionUtil
import pl.yalgrin.playnite.simplesync.util.panel.PanelFeatureFactoryUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class PanelFeatureResourceTest extends AbstractPanelObjectTest<Feature, PanelFeatureDTO> {

    @Autowired
    private FeatureRepository featureRepository

    def "save single feature"() {
        given:
        PanelFeatureDTO dto = PanelFeatureFactoryUtil.createFeature("test")
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def response = makeSaveRequest(dto)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, PanelFeatureDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId.set(it.id)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, newObjectId.get())
    }

    def "save multiple features"() {
        given:
        List<PanelFeatureDTO> list = new ArrayList<>()
        for (int i = 0; i < 200; i++) {
            list.add(PanelFeatureFactoryUtil.featureWithIndex(i))
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
            StepVerifier.create(IntegrationTestUtil.getReturnMono(tuple.getV1(), PanelFeatureDTO.class))
                    .expectNextMatches {
                        assert it.id != null
                        createdIds.add(it.id)
                        objectMatches(it, list.get(tuple.getV2()))
                    }
                    .verifyComplete()
            true
        }

        and:
        list.withIndex().stream().allMatch { tuple -> assertEntityAndGetResponse(tuple.getV1(), createdIds.get(tuple.getV2())) }
    }

    def "save feature and then delete it"() {
        given:
        PanelFeatureDTO dto = PanelFeatureFactoryUtil.randomFeature()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, PanelFeatureDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId.set(it.id)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, newObjectId.get())

        when:
        dto.id = newObjectId.get()
        def deleteResponse = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, PanelFeatureDTO.class))
                .verifyComplete()
        assertDeleted(newObjectId.get())
    }

    def "save and then remove repeatedly"() {
        given:
        PanelFeatureDTO dto = PanelFeatureFactoryUtil.randomFeature()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, PanelFeatureDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId.set(it.id)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, newObjectId.get())

        when:
        dto.id = newObjectId.get()
        def deleteResponse = makeDeleteRequest(dto)
        def deleteResponse2 = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()
        deleteResponse2.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, PanelFeatureDTO.class))
                .verifyComplete()
        assertDeleted(newObjectId.get())
    }

    def "save, remove, then save a new one with the same name"() {
        given:
        PanelFeatureDTO dto = PanelFeatureFactoryUtil.randomFeature()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, PanelFeatureDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId.set(it.id)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, newObjectId.get())

        when:
        dto.id = newObjectId.get()
        def deleteResponse = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, PanelFeatureDTO.class))
                .verifyComplete()
        assertDeleted(newObjectId.get())

        when:
        PanelFeatureDTO newDto = PanelFeatureFactoryUtil.randomFeature()
        newDto.name = dto.name
        AtomicLong newObjectId2 = new AtomicLong(-1)

        def newSaveResponse = makeSaveRequest(newDto)

        then:
        newSaveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(newSaveResponse, PanelFeatureDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId2.set(it.id)
                    assert newObjectId.get() == newObjectId2.get()
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, newObjectId2.get())
    }


    def "save 2, remove one, then rename the other to the same name"() {
        given:
        PanelFeatureDTO dto1 = PanelFeatureFactoryUtil.randomFeature()
        PanelFeatureDTO dto2 = PanelFeatureFactoryUtil.randomFeature()
        AtomicLong newObjectId = new AtomicLong(-1)
        AtomicLong newObjectId2 = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto1)
        def saveResponse2 = makeSaveRequest(dto2)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, PanelFeatureDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId.set(it.id)
                    objectMatches(it, dto1)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto1, newObjectId.get())

        and:
        saveResponse2.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse2, PanelFeatureDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId2.set(it.id)
                    objectMatches(it, dto2)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto2, newObjectId2.get())

        when:
        dto1.id = newObjectId.get()
        def deleteResponse = makeDeleteRequest(dto1)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, PanelFeatureDTO.class))
                .verifyComplete()
        assertDeleted(newObjectId.get())

        when:
        dto2.id = newObjectId2.get()
        dto2.name = dto1.name

        def newSaveResponse = makeSaveRequest(dto2)

        then:
        newSaveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(newSaveResponse, PanelFeatureDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    assert it.id == newObjectId2.get()
                    objectMatches(it, dto2)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto2, newObjectId2.get())
    }

    def "save, modify and delete and await the change stream"() {
        given:
        PanelFeatureDTO toSave = PanelFeatureFactoryUtil.randomFeature()
        PanelFeatureDTO modified = PanelFeatureFactoryUtil.createFeature("some other name")

        when:
        def changeRequest = makeConnectRequest(otherClientInfo)
        def responseFlux = changeRequest.returnResult(new ParameterizedTypeReference<String>() {}).responseBody

        then:
        List<Long> collectedIds = Collections.synchronizedList(new ArrayList<>())
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
                            .expectBody(PanelFeatureDTO.class)
                            .returnResult()
                            .responseBody
                    PanelFeatureAssertionUtil.assertFeature(toSave, result)
                    assert result.id != null
                    collectedIds.add(result.id)
                    if (collectedIds.size() > 1) {
                        assert collectedIds.stream().distinct().size() == 1
                    }
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.FEATURE
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PanelFeatureDTO.class))
                            .expectNextMatches {
                                assert it.id == collectedIds.first
                                objectMatches(it, toSave)
                            }
                            .verifyComplete()
                }
                .then {
                    modified.id = collectedIds.first
                    def result = makeSaveRequest(modified).expectStatus().is2xxSuccessful()
                            .expectBody(PanelFeatureDTO.class)
                            .returnResult()
                            .responseBody
                    PanelFeatureAssertionUtil.assertFeature(modified, result)
                    assert result.id == collectedIds.first
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.FEATURE
                    assert change.getClientId() == clientId
                    assert change.getObjectId() == collectedIds.first
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(collectedIds.first)

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PanelFeatureDTO.class))
                            .expectNextMatches {
                                assert it.id == collectedIds.first
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
                    assert change.getType() == ObjectType.FEATURE
                    assert change.getClientId() == clientId
                    assert change.getObjectId() == collectedIds.first
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(collectedIds.first)

                    getResponse.expectStatus().is4xxClientError()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PanelFeatureDTO.class))
                            .verifyComplete()
                }
                .thenCancel()
                .verify()
    }

    @Override
    protected String uri() {
        return "/api/panel/feature"
    }

    @Override
    protected ObjectRepository<Feature> repository() {
        return featureRepository
    }

    @Override
    protected Class<? extends PanelFeatureDTO> dtoClass() {
        return PanelFeatureDTO.class
    }

    @Override
    boolean objectMatches(PanelFeatureDTO resultDTO, PanelFeatureDTO expectedDTO) {
        PanelFeatureAssertionUtil.assertFeature(expectedDTO, resultDTO)
    }

    @Override
    boolean objectMatches(Feature resultDTO, PanelFeatureDTO expectedDTO) {
        PanelFeatureAssertionUtil.assertFeatureEntity(expectedDTO, resultDTO)
    }
}
