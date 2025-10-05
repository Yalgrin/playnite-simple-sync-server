package pl.yalgrin.playnite.simplesync.web.objects

import io.vavr.Tuple
import io.vavr.Tuple2
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.domain.objects.Game
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.dto.objects.GameDTO
import pl.yalgrin.playnite.simplesync.enums.ObjectType
import pl.yalgrin.playnite.simplesync.repository.objects.GameRepository
import pl.yalgrin.playnite.simplesync.repository.objects.ObjectRepository
import pl.yalgrin.playnite.simplesync.service.MetadataService
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.objects.GameAssertionUtil
import pl.yalgrin.playnite.simplesync.util.objects.GameFactoryUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong

class GameResourceTest extends AbstractObjectWithDiffTest<Game, GameDTO> {

    @Autowired
    private GameRepository gameRepository

    def "save single game"() {
        given:
        GameDTO dto = GameFactoryUtil.createGame(UUID.randomUUID().toString(), "test")
        def icon = GameFactoryUtil.randomFile("Icon.png", 2048)
        def coverImage = GameFactoryUtil.randomFile("CoverImage.jpeg", 2048)
        def backgroundImage = GameFactoryUtil.randomFile("BackgroundImage.tif", 2048)
        def files = List.of(icon, coverImage, backgroundImage)

        when:
        def response = makeSaveRequest(dto, files)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, GameDTO.class))
                .expectNextMatches { objectMatches(it, dto) }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, files)
    }

    def "save multiple games"() {
        given:
        List<Tuple2<GameDTO, List<MultipartFile>>> list = new ArrayList<>()
        for (int i = 0; i < 1000; i++) {
            list.add(Tuple.of(GameFactoryUtil.gameWithIndex(i), GameFactoryUtil.randomFiles()))
        }

        when:
        List<CompletableFuture<WebTestClient.ResponseSpec>> futures = list.stream()
                .map { tuple -> CompletableFuture.supplyAsync({ makeSaveRequest(tuple._1, tuple._2) }) }
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
            StepVerifier.create(IntegrationTestUtil.getReturnMono(tuple.getV1(), GameDTO.class))
                    .expectNextMatches { objectMatches(it, list.get(tuple.getV2())._1()) }
                    .verifyComplete()
            true
        }

        and:
        list.stream().allMatch { tuple -> assertEntityAndGetResponse(tuple._1(), tuple._2()) }
    }

    def "save game and then delete it"() {
        given:
        GameDTO dto = GameFactoryUtil.randomGame()
        def files = GameFactoryUtil.randomFiles()

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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, GameDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save and then remove repeatedly"() {
        given:
        GameDTO dto = GameFactoryUtil.randomGame()
        def files = GameFactoryUtil.randomFiles()

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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, GameDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save, modify and delete and await the change stream"() {
        given:
        GameDTO toSave = GameFactoryUtil.randomGame()
        def files = GameFactoryUtil.randomFiles()
        GameDTO modified = toSave.toBuilder().name("some other name").build()
        GameDTO removed = modified.toBuilder().removed(true).build()

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
                .thenConsumeWhile { change ->
                    change.getType() != ObjectType.Game
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.Game
                    assert change.getClientId() == "test"
                    assert change.getObjectId() != null
                    assert !change.isForceFetch()
                    newObjectId.set(change.getObjectId())
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDTO.class))
                            .expectNextMatches { objectMatches(it, toSave) }
                            .verifyComplete()
                }
                .then {
                    makeSaveRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.GameDiff
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get() + 1
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDTO.class))
                            .expectNextMatches { objectMatches(it, modified) }
                            .verifyComplete()
                }
                .then {
                    makeDeleteRequest(modified).expectStatus().is2xxSuccessful()
                }
                .expectNextMatches { change ->
                    assert change.getId() != null
                    assert change.getType() == ObjectType.Game
                    assert change.getClientId() == "test"
                    assert change.getObjectId() == newObjectId.get()
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(newObjectId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDTO.class))
                            .expectNextMatches { objectMatches(it, removed) }
                            .verifyComplete()
                }
                .thenCancel()
                .verify()
    }

    @Override
    protected String uri() {
        return "/api/game"
    }

    @Override
    protected ObjectRepository<Game> repository() {
        return gameRepository
    }

    @Override
    protected Class<? extends GameDTO> dtoClass() {
        return GameDTO.class
    }

    @Override
    boolean objectMatches(GameDTO resultDTO, GameDTO expectedDTO) {
        GameAssertionUtil.assertGame(expectedDTO, resultDTO)
    }

    @Override
    boolean objectMatches(Game resultDTO, GameDTO expectedDTO) {
        GameAssertionUtil.assertGameEntity(expectedDTO, resultDTO)
    }

    protected assertEntityAndGetResponse(GameDTO dto, List<MultipartFile> files) {
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
                        .path("/api/game-metadata/$id/$metadataName")
                        .build())
                .exchange()
    }
}
