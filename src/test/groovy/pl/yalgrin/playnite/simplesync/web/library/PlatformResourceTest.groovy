package pl.yalgrin.playnite.simplesync.web.library

import io.vavr.Tuple
import io.vavr.Tuple2
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.client.enums.MessageType
import pl.yalgrin.playnite.simplesync.client.message.ChangeMessage
import pl.yalgrin.playnite.simplesync.client.message.InitializationMessage
import pl.yalgrin.playnite.simplesync.common.config.ConstantsKt
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType
import pl.yalgrin.playnite.simplesync.library.domain.Platform
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDTO
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDTO
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.library.repository.PlatformRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.JsonMapperUtil
import pl.yalgrin.playnite.simplesync.util.library.GameFactoryUtil
import pl.yalgrin.playnite.simplesync.util.library.PlatformAssertionUtil
import pl.yalgrin.playnite.simplesync.util.library.PlatformFactoryUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class PlatformResourceTest extends AbstractObjectWithDiffTest<Platform, PlatformDTO> {

    @Autowired
    private PlatformRepository platformRepository

    def "save single platform"() {
        given:
        PlatformDTO dto = PlatformFactoryUtil.createPlatform(UUID.randomUUID().toString(), "test")
        def icon = GameFactoryUtil.randomFile("Icon.png", 2048)
        def coverImage = GameFactoryUtil.randomFile("CoverImage.jpeg", 2048)
        def backgroundImage = GameFactoryUtil.randomFile("BackgroundImage.tif", 2048)
        def files = List.of(backgroundImage, coverImage, icon)
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def response = makeSaveRequest(dto, files)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, PlatformDTO.class))
                .expectNextMatches {
                    assert it.serverId != null
                    newObjectId.set(it.serverId)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, files, newObjectId.get())
    }

    def "save multiple platforms"() {
        given:
        List<Tuple2<PlatformDTO, List<MultipartFile>>> list = new ArrayList<>()
        for (int i = 0; i < 1000; i++) {
            list.add(Tuple.of(PlatformFactoryUtil.platformWithIndex(i), PlatformFactoryUtil.randomFiles()))
        }
        List<Long> createdIds = new ArrayList<>()

        when:
        List<CompletableFuture<WebTestClient.ResponseSpec>> futures = list.stream()
                .map { tuple -> CompletableFuture.supplyAsync({ makeSaveRequest(tuple._1(), tuple._2()) }) }
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
            StepVerifier.create(IntegrationTestUtil.getReturnMono(tuple.getV1(), PlatformDTO.class))
                    .expectNextMatches {
                        assert it.serverId != null
                        createdIds.add(it.serverId)
                        objectMatches(it, list.get(tuple.getV2())._1())
                    }
                    .verifyComplete()
            true
        }

        and:
        list.withIndex().stream().allMatch { tuple -> assertEntityAndGetResponse(tuple.getV1()._1(), tuple.getV1()._2(), createdIds.get(tuple.getV2())) }
    }

    def "save platform and then delete it"() {
        given:
        PlatformDTO dto = PlatformFactoryUtil.randomPlatform()
        def files = PlatformFactoryUtil.randomFiles()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, PlatformDTO.class))
                .expectNextMatches {
                    assert it.serverId != null
                    newObjectId.set(it.serverId)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, files, newObjectId.get())

        when:
        def deleteResponse = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, PlatformDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save and then remove repeatedly"() {
        given:
        PlatformDTO dto = PlatformFactoryUtil.randomPlatform()
        def files = PlatformFactoryUtil.randomFiles()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, PlatformDTO.class))
                .expectNextMatches {
                    assert it.serverId != null
                    newObjectId.set(it.serverId)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, files, newObjectId.get())

        when:
        def deleteResponse = makeDeleteRequest(dto)
        def deleteResponse2 = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()
        deleteResponse2.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, PlatformDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save, modify twice and delete and await the change stream"() {
        given:
        PlatformDTO toSave = PlatformFactoryUtil.randomPlatform()
        def files = PlatformFactoryUtil.randomFiles(100, 100, 100)
        PlatformDTO modified = toSave.withName("some other name")
        PlatformDiffDTO modifiedViaDiffDTO = new PlatformDiffDTO()
        modifiedViaDiffDTO.id = modified.id
        modifiedViaDiffDTO.name = "even different name"
        modifiedViaDiffDTO.specificationId = "123"
        modifiedViaDiffDTO.changedFields = List.of("Name", "SpecificationId")
        PlatformDTO removed = modified.withRemoved(true)
        removed.name = modifiedViaDiffDTO.name
        removed.specificationId = modifiedViaDiffDTO.specificationId

        when:
        def changeRequest = makeConnectRequest(otherClientInfo)
        def responseFlux = changeRequest.returnResult(new ParameterizedTypeReference<String>() {}).responseBody

        then:
        List<Long> collectedIds = Collections.synchronizedList(new ArrayList<>())
        AtomicLong newDiffObjectId = new AtomicLong(-1)
        AtomicLong newSecondDiffObjectId = new AtomicLong(-1)
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
                    def result = makeSaveRequest(toSave, files).expectStatus().is2xxSuccessful()
                            .expectBody(PlatformDTO.class)
                            .returnResult()
                            .responseBody
                    PlatformAssertionUtil.assertPlatform(toSave, result)
                    assert result.serverId != null
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
                    assert change.getType() == ObjectType.PLATFORM
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PlatformDTO.class))
                            .expectNextMatches { objectMatches(it, toSave) }
                            .verifyComplete()
                }
                .then {
                    def result = makeSaveRequest(modified, files).expectStatus().is2xxSuccessful()
                            .expectBody(PlatformDTO.class)
                            .returnResult()
                            .responseBody
                    PlatformAssertionUtil.assertPlatform(modified, result)
                    assert result.serverId == collectedIds.first
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.PLATFORM_DIFF
                    assert change.getClientId() == clientId
                    assert change.getObjectId() != null
                    assert change.getDiffParentObjectId() == collectedIds.first
                    assert !change.isForceFetch()
                    newDiffObjectId.set(change.getObjectId())
                    true
                }
                .then {
                    def getResponse = makeGetRequest(collectedIds.first)

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PlatformDTO.class))
                            .expectNextMatches {
                                objectMatches(it, modified)
                                assert it.serverId == collectedIds.first
                                true
                            }
                            .verifyComplete()
                }
                .then {
                    assert newDiffObjectId.get() != -1
                    def getResponse = makeGetDiffRequest(newDiffObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PlatformDiffDTO.class))
                            .expectNextMatches {
                                assert it.changedFields != null
                                assert it.changedFields.size() == 1
                                assert it.changedFields.contains("Name")
                                assert it.name == modified.name
                                true
                            }
                            .verifyComplete()
                }
                .then {
                    def result = makeSaveDiffRequest(modifiedViaDiffDTO, List.of()).expectStatus().is2xxSuccessful()
                            .expectBody(PlatformDTO.class)
                            .returnResult()
                            .responseBody
                    assert result.name == modifiedViaDiffDTO.name
                    assert result.specificationId == modifiedViaDiffDTO.specificationId
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.PLATFORM_DIFF
                    assert change.getClientId() == clientId
                    assert change.getObjectId() != null
                    assert change.getDiffParentObjectId() == collectedIds.first
                    assert !change.isForceFetch()
                    newSecondDiffObjectId.set(change.getObjectId())
                    true
                }
                .then {
                    assert newDiffObjectId.get() != -1
                    def getResponse = makeGetDiffRequest(newSecondDiffObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PlatformDiffDTO.class))
                            .expectNextMatches {
                                assert it.changedFields != null
                                assert it.changedFields.size() == 2
                                assert it.changedFields.contains("Name")
                                assert it.changedFields.contains("SpecificationId")
                                assert it.name == modifiedViaDiffDTO.name
                                assert it.specificationId == modifiedViaDiffDTO.specificationId
                                true
                            }
                            .verifyComplete()
                }
                .then {
                    makeDeleteRequest(removed).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.PLATFORM
                    assert change.getClientId() == clientId
                    assert change.getObjectId() == collectedIds.first
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(collectedIds.first)

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PlatformDTO.class))
                            .expectNextMatches {
                                objectMatches(it, removed)
                                assert it.serverId == collectedIds.first
                                true
                            }
                            .verifyComplete()
                }
                .thenCancel()
                .verify()
    }

    @Override
    protected String uri() {
        return "/api/platform"
    }

    protected static String diffUri() {
        return "/api/platform-diff"
    }

    @Override
    protected ObjectRepository<Platform> repository() {
        return platformRepository
    }

    @Override
    protected Class<? extends PlatformDTO> dtoClass() {
        return PlatformDTO.class
    }

    @Override
    boolean objectMatches(PlatformDTO resultDTO, PlatformDTO expectedDTO) {
        PlatformAssertionUtil.assertPlatform(expectedDTO, resultDTO)
    }

    @Override
    boolean objectMatches(Platform resultDTO, PlatformDTO expectedDTO) {
        PlatformAssertionUtil.assertPlatformEntity(expectedDTO, resultDTO)
    }

    protected assertEntityAndGetResponse(PlatformDTO dto, List<MultipartFile> files, Long id) {
        def savedEntities = repository().findByPlayniteId(dto.id).collectList().block()
        assert savedEntities.size() == 1

        def savedEntity = savedEntities[0]
        assert objectMatches(savedEntity, dto)

        def getResponse = makeGetRequest(savedEntity.getId())

        getResponse.expectStatus().is2xxSuccessful()

        StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, dtoClass()))
                .expectNextMatches {
                    assert it.serverId == id
                    objectMatches(it, dto)
                }
                .verifyComplete()

        Map<String, MultipartFile> expectedFileMap = new HashMap<>()
        for (final def file in files) {
            expectedFileMap.put(FilenameUtils.getBaseName(file.getName()), file)
        }
        for (final def filename in ConstantsKt.ALLOWED_FILE_NAMES) {
            def expectedFile = expectedFileMap.get(filename)
            def metadataRequest = makeGetMetadataRequest(savedEntity.getId(), filename)
            if (expectedFile != null) {
                metadataRequest.expectStatus().is2xxSuccessful()

                def response = metadataRequest.expectBody().returnResult()
                assert response.getResponseBody() != null
                assert response.getResponseBody() == expectedFile.getBytes()
                assert response.getResponseHeaders().getContentType() == MediaType.parseMediaType(expectedFile.getContentType())
            } else {
                metadataRequest.expectStatus().isNotFound()
            }
        }

        true
    }

    protected WebTestClient.ResponseSpec makeGetMetadataRequest(Long id, String metadataName) {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/platform-metadata/$id/$metadataName")
                        .build())
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeSaveDiffRequest(PlatformDiffDTO dto, List<MultipartFile> files) {
        def builder = new MultipartBodyBuilder()
        builder.part("dto", dto)
        if (!files.isEmpty()) {
            files.each { file ->
                builder.part("files", new ByteArrayResource(file.getBytes()) {
                    @Override
                    String getFilename() {
                        return file.getOriginalFilename()
                    }
                })
            }
        }
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("${diffUri()}/save")
                        .queryParam("clientId", "test")
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeGetDiffRequest(Long id) {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("${diffUri()}/$id")
                        .build())
                .exchange()
    }
}
