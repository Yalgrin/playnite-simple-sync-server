package pl.yalgrin.playnite.simplesync.web.objects

import io.vavr.Tuple
import io.vavr.Tuple2
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.domain.objects.Platform
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.dto.objects.PlatformDTO
import pl.yalgrin.playnite.simplesync.enums.ObjectType
import pl.yalgrin.playnite.simplesync.repository.objects.ObjectRepository
import pl.yalgrin.playnite.simplesync.repository.objects.PlatformRepository
import pl.yalgrin.playnite.simplesync.service.MetadataService
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.objects.GameFactoryUtil
import pl.yalgrin.playnite.simplesync.util.objects.PlatformAssertionUtil
import pl.yalgrin.playnite.simplesync.util.objects.PlatformFactoryUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong

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

        when:
        def response = makeSaveRequest(dto, files)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, PlatformDTO.class))
                .expectNextMatches { objectMatches(it, dto) }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, files)
    }

    def "save multiple platforms"() {
        given:
        List<Tuple2<PlatformDTO, List<MultipartFile>>> list = new ArrayList<>()
        for (int i = 0; i < 1000; i++) {
            list.add(Tuple.of(PlatformFactoryUtil.platformWithIndex(i), PlatformFactoryUtil.randomFiles()))
        }

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
                    .expectNextMatches { objectMatches(it, list.get(tuple.getV2())._1()) }
                    .verifyComplete()
            true
        }

        and:
        list.stream().allMatch { tuple -> assertEntityAndGetResponse(tuple._1(), tuple._2()) }
    }

    def "save platform and then delete it"() {
        given:
        PlatformDTO dto = PlatformFactoryUtil.randomPlatform()
        def files = PlatformFactoryUtil.randomFiles()

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        assertEntityAndGetResponse(dto, files)

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

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        assertEntityAndGetResponse(dto, files)

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

    def "save, modify and delete and await the change stream"() {
        given:
        PlatformDTO toSave = PlatformFactoryUtil.randomPlatform()
        def files = PlatformFactoryUtil.randomFiles()
        PlatformDTO modified = toSave.toBuilder().name("some other name").build()
        PlatformDTO removed = modified.toBuilder().removed(true).build()

        when:
        def changeRequest = makeChangeStreamRequest()
        def responseFlux = changeRequest.returnResult(ChangeDTO.class).responseBody

        then:
        AtomicLong newObjectId = new AtomicLong(-1)
        StepVerifier.create(responseFlux)
                .expectSubscription()
                .then {
                    makeSaveRequest(toSave, files).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.Platform
                    assert change.getClientId() == "test"
                    assert change.getObjectId() != null
                    assert !change.isForceFetch()
                    newObjectId.set(change.getObjectId())
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PlatformDTO.class))
                            .expectNextMatches { objectMatches(it, toSave) }
                            .verifyComplete()
                }
                .then {
                    makeSaveRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.PlatformDiff
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get() + 1
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PlatformDTO.class))
                            .expectNextMatches { objectMatches(it, modified) }
                            .verifyComplete()
                }
                .then {
                    makeDeleteRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.Platform
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PlatformDTO.class))
                            .expectNextMatches { objectMatches(it, removed) }
                            .verifyComplete()
                }
                .thenCancel()
                .verify()
    }

    @Override
    protected String uri() {
        return "/api/platform"
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

    protected assertEntityAndGetResponse(PlatformDTO dto, List<MultipartFile> files) {
        def savedEntities = repository().findByPlayniteId(dto.id).collectList().block()
        assert savedEntities.size() == 1

        def savedEntity = savedEntities[0]
        assert objectMatches(savedEntity, dto)

        def getResponse = makeGetRequest(savedEntity.getId())

        getResponse.expectStatus().is2xxSuccessful()

        StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, dtoClass()))
                .expectNextMatches { objectMatches(it, dto) }
                .verifyComplete()

        Map<String, MultipartFile> expectedFileMap = new HashMap<>()
        for (final def file in files) {
            expectedFileMap.put(FilenameUtils.getBaseName(file.getName()), file)
        }
        for (final def filename in MetadataService.ALLOWED_FILE_NAMES) {
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
}
