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
import pl.yalgrin.playnite.simplesync.library.domain.LibraryPlugin
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDTO
import pl.yalgrin.playnite.simplesync.library.dto.LibraryPluginDiffDTO
import pl.yalgrin.playnite.simplesync.library.repository.LibraryPluginRepository
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.JsonMapperUtil
import pl.yalgrin.playnite.simplesync.util.library.GameFactoryUtil
import pl.yalgrin.playnite.simplesync.util.library.LibraryPluginAssertionUtil
import pl.yalgrin.playnite.simplesync.util.library.LibraryPluginFactoryUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class LibraryPluginResourceTest extends AbstractObjectWithDiffTest<LibraryPlugin, LibraryPluginDTO> {

    @Autowired
    private LibraryPluginRepository pluginRepository

    def "save single plugin"() {
        given:
        LibraryPluginDTO dto = LibraryPluginFactoryUtil.createLibraryPlugin(UUID.randomUUID().toString(), "test")
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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, LibraryPluginDTO.class))
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
        assertEntityAndGetResponse(dto, files, newObjectId.get())
    }

    def "save multiple plugins"() {
        given:
        List<Tuple2<LibraryPluginDTO, List<MultipartFile>>> list = new ArrayList<>()
        for (int i = 0; i < 200; i++) {
            list.add(Tuple.of(LibraryPluginFactoryUtil.pluginWithIndex(i), LibraryPluginFactoryUtil.randomFiles()))
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
            StepVerifier.create(IntegrationTestUtil.getReturnMono(tuple.getV1(), LibraryPluginDTO.class))
                    .expectNextMatches {
                        assert it.serverId != null
                        assert it.createdAt != null
                        assert it.createdBy != null
                        assert it.modifiedAt != null
                        assert it.modifiedBy != null
                        createdIds.add(it.serverId)
                        objectMatches(it, list.get(tuple.getV2())._1())
                    }
                    .verifyComplete()
            true
        }

        and:
        list.withIndex().stream().allMatch { tuple -> assertEntityAndGetResponse(tuple.getV1()._1(), tuple.getV1()._2(), createdIds.get(tuple.getV2())) }
    }

    def "save plugin and then delete it"() {
        given:
        LibraryPluginDTO dto = LibraryPluginFactoryUtil.randomLibraryPlugin()
        def files = LibraryPluginFactoryUtil.randomFiles()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, LibraryPluginDTO.class))
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
        assertEntityAndGetResponse(dto, files, newObjectId.get())

        when:
        def deleteResponse = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, LibraryPluginDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save and then remove repeatedly"() {
        given:
        LibraryPluginDTO dto = LibraryPluginFactoryUtil.randomLibraryPlugin()
        def files = LibraryPluginFactoryUtil.randomFiles()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, LibraryPluginDTO.class))
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
        assertEntityAndGetResponse(dto, files, newObjectId.get())

        when:
        def deleteResponse = makeDeleteRequest(dto)
        def deleteResponse2 = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()
        deleteResponse2.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, LibraryPluginDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save, modify twice and delete and await the change stream"() {
        given:
        LibraryPluginDTO toSave = LibraryPluginFactoryUtil.randomLibraryPlugin()
        def files = LibraryPluginFactoryUtil.randomFiles(100, 100)
        LibraryPluginDTO modified = toSave.withName("some other name")
        LibraryPluginDiffDTO modifiedViaDiffDTO = new LibraryPluginDiffDTO()
        modifiedViaDiffDTO.id = modified.id
        modifiedViaDiffDTO.name = "even different name"
        modifiedViaDiffDTO.changedFields = List.of("Name")
        LibraryPluginDTO removed = modified.withRemoved(true)
        removed.name = modifiedViaDiffDTO.name

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
                            .expectBody(LibraryPluginDTO.class)
                            .returnResult()
                            .responseBody
                    LibraryPluginAssertionUtil.assertLibraryPlugin(toSave, result)
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
                    assert change.getType() == ObjectType.LIBRARY_PLUGIN
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, LibraryPluginDTO.class))
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
                    def result = makeSaveRequest(modified, files).expectStatus().is2xxSuccessful()
                            .expectBody(LibraryPluginDTO.class)
                            .returnResult()
                            .responseBody
                    LibraryPluginAssertionUtil.assertLibraryPlugin(modified, result)
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
                    assert change.getType() == ObjectType.LIBRARY_PLUGIN_DIFF
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, LibraryPluginDTO.class))
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
                    assert newDiffObjectId.get() != -1
                    def getResponse = makeGetDiffRequest(newDiffObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, LibraryPluginDiffDTO.class))
                            .expectNextMatches {
                                assert it.serverId != null
                                assert it.changedFields != null
                                assert it.changedFields.size() == 1
                                assert it.changedFields.contains("Name")
                                assert it.name == modified.name
                                assert it.createdAt != null
                                assert it.createdBy != null
                                true
                            }
                            .verifyComplete()
                }
                .then {
                    def result = makeSaveDiffRequest(modifiedViaDiffDTO, List.of()).expectStatus().is2xxSuccessful()
                            .expectBody(LibraryPluginDTO.class)
                            .returnResult()
                            .responseBody
                    assert result.name == modifiedViaDiffDTO.name
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
                    assert change.getType() == ObjectType.LIBRARY_PLUGIN_DIFF
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, LibraryPluginDiffDTO.class))
                            .expectNextMatches {
                                assert it.serverId != null
                                assert it.changedFields != null
                                assert it.changedFields.size() == 1
                                assert it.changedFields.contains("Name")
                                assert it.name == modifiedViaDiffDTO.name
                                assert it.createdAt != null
                                assert it.createdBy != null
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
                    assert change.getType() == ObjectType.LIBRARY_PLUGIN
                    assert change.getClientId() == clientId
                    assert change.getObjectId() == collectedIds.first
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(collectedIds.first)

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, LibraryPluginDTO.class))
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
        return "/api/library-plugin"
    }

    protected static String diffUri() {
        return "/api/library-plugin-diff"
    }

    @Override
    protected ObjectRepository<LibraryPlugin> repository() {
        return pluginRepository
    }

    @Override
    protected Class<? extends LibraryPluginDTO> dtoClass() {
        return LibraryPluginDTO.class
    }

    @Override
    boolean objectMatches(LibraryPluginDTO resultDTO, LibraryPluginDTO expectedDTO) {
        LibraryPluginAssertionUtil.assertLibraryPlugin(expectedDTO, resultDTO)
    }

    @Override
    boolean objectMatches(LibraryPlugin resultDTO, LibraryPluginDTO expectedDTO) {
        LibraryPluginAssertionUtil.assertLibraryPluginEntity(expectedDTO, resultDTO)
    }

    protected assertEntityAndGetResponse(LibraryPluginDTO dto, List<MultipartFile> files, Long id) {
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
        for (final def filename in ConstantsKt.ALLOWED_FILE_NAMES_PLUGIN) {
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
                        .path("/api/library-plugin-metadata/$id/$metadataName")
                        .build())
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeSaveDiffRequest(LibraryPluginDiffDTO dto, List<MultipartFile> files) {
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
