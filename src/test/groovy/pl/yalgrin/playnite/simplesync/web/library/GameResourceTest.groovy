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
import pl.yalgrin.playnite.simplesync.helper.MetadataTestHelper
import pl.yalgrin.playnite.simplesync.library.domain.Game
import pl.yalgrin.playnite.simplesync.library.dto.*
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.JsonMapperUtil
import pl.yalgrin.playnite.simplesync.util.library.GameAssertionUtil
import pl.yalgrin.playnite.simplesync.util.library.GameFactoryUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class GameResourceTest extends AbstractObjectWithDiffTest<Game, GameDTO> {

    @Autowired
    private GameRepository gameRepository
    @Autowired
    private MetadataTestHelper metadataTestHelper

    def "save single game"() {
        given:
        GameDTO dto = GameFactoryUtil.createGame(UUID.randomUUID().toString(), "test")
        AtomicLong newObjectId = new AtomicLong(-1)
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

        and:
        checkFiles(dto, { id ->
            assert metadataTestHelper.fileExists(ConstantsKt.GAME, id, "Icon.png")
            assert metadataTestHelper.fileExists(ConstantsKt.GAME, id, "CoverImage.jpeg")
            assert metadataTestHelper.fileExists(ConstantsKt.GAME, id, "BackgroundImage.tif")
        })
    }

    def "save multiple games"() {
        given:
        List<Tuple2<GameDTO, List<MultipartFile>>> list = new ArrayList<>()
        for (int i = 0; i < 100; i++) {
            def game = GameFactoryUtil.gameWithIndex(i)
            def files = GameFactoryUtil.randomFiles()
            game.setHasIcon(files.any { it.name.startsWith("Icon") })
            game.setHasCoverImage(files.any { it.name.startsWith("CoverImage") })
            game.setHasBackgroundImage(files.any { it.name.startsWith("BackgroundImage") })
            list.add(Tuple.of(game, files))
        }
        List<Long> createdIds = new ArrayList<>()

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

    def "save game and then delete it"() {
        given:
        GameDTO dto = GameFactoryUtil.randomGame()
        def files = GameFactoryUtil.randomFiles()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, GameDTO.class))
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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, GameDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save and then remove repeatedly"() {
        given:
        GameDTO dto = GameFactoryUtil.randomGame()
        def files = GameFactoryUtil.randomFiles()
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, GameDTO.class))
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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, GameDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save, modify twice and delete and await the change stream"() {
        given:
        GameDTO toSave = GameFactoryUtil.randomGame()
        def files = GameFactoryUtil.randomFiles(100, 100, 100)
        GameDTO modified = toSave.withName("some other name")
        modified.setDescription("Description")
        def newGenre = new GenreDTO()
        newGenre.id = UUID.randomUUID().toString()
        newGenre.name = "New genre"
        modified.genres = List.of(newGenre)
        def newSource = new SourceDTO()
        newSource.id = UUID.randomUUID().toString()
        newSource.name = "New source"
        modified.source = newSource
        GameDiffDTO modifiedViaDiffDTO = new GameDiffDTO()
        modifiedViaDiffDTO.id = modified.id
        modifiedViaDiffDTO.gameId = modified.gameId
        modifiedViaDiffDTO.pluginId = modified.pluginId
        modifiedViaDiffDTO.name = "even different name"
        modifiedViaDiffDTO.version = "3.0"
        def newCategory = new CategoryDTO()
        newCategory.id = UUID.randomUUID().toString()
        newCategory.name = "New category"
        modifiedViaDiffDTO.categories = List.of(newCategory)
        def newSecondSource = new SourceDTO()
        newSecondSource.id = UUID.randomUUID().toString()
        newSecondSource.name = "New source 2"
        modifiedViaDiffDTO.source = newSecondSource
        modifiedViaDiffDTO.changedFields = List.of("Name", "Version", "Categories", "Source")
        GameDTO removed = modified.withRemoved(true)
        removed.name = modifiedViaDiffDTO.name
        removed.version = modifiedViaDiffDTO.version
        removed.categories = modifiedViaDiffDTO.categories
        removed.source = modifiedViaDiffDTO.source

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
                            .expectBody(GameDTO.class)
                            .returnResult()
                            .responseBody
                    GameAssertionUtil.assertGame(toSave, result)
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
                .thenConsumeWhile { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    change.getType() != ObjectType.GAME
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.GAME
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDTO.class))
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
                            .expectBody(GameDTO.class)
                            .returnResult()
                            .responseBody
                    GameAssertionUtil.assertGame(modified, result)
                    assert result.serverId == collectedIds.first
                    assert result.createdAt != null
                    assert result.createdBy != null
                    assert result.modifiedAt != null
                    assert result.modifiedBy != null
                }
                .thenConsumeWhile { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    change.getType() != ObjectType.GAME_DIFF
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.GAME_DIFF
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDTO.class))
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDiffDTO.class))
                            .expectNextMatches {
                                assert it.serverId != null
                                assert it.changedFields != null
                                assert it.changedFields.size() == 4
                                assert it.changedFields.contains("Name")
                                assert it.changedFields.contains("Description")
                                assert it.changedFields.contains("Genres")
                                assert it.changedFields.contains("Source")
                                assert it.name == modified.name
                                assert it.description == modified.description
                                assert it.version == null
                                def genres = it.genres
                                assert genres != null
                                assert genres.size() == 1
                                assert genres.get(0).id == newGenre.id
                                assert genres.get(0).name == newGenre.name
                                assert genres.get(0).createdAt == null
                                assert genres.get(0).createdBy == null
                                assert genres.get(0).modifiedAt == null
                                assert genres.get(0).modifiedBy == null
                                assert it.source?.id == newSource.id
                                assert it.source?.name == newSource.name
                                assert it.source?.createdAt == null
                                assert it.source?.createdBy == null
                                assert it.source?.modifiedAt == null
                                assert it.source?.modifiedBy == null
                                assert it.createdAt != null
                                assert it.createdBy != null
                                true
                            }
                            .verifyComplete()
                }
                .then {
                    def result = makeSaveDiffRequest(modifiedViaDiffDTO, List.of()).expectStatus().is2xxSuccessful()
                            .expectBody(GameDTO.class)
                            .returnResult()
                            .responseBody
                    assert result.name == modifiedViaDiffDTO.name
                    assert result.description == modified.description
                    assert result.version == modifiedViaDiffDTO.version
                    assert result.createdAt != null
                    assert result.createdBy != null
                    assert result.modifiedAt != null
                    assert result.modifiedBy != null
                }
                .thenConsumeWhile { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    change.getType() != ObjectType.GAME && change.getType() != ObjectType.GAME_DIFF
                }
                .expectNextMatches { str ->
                    def change = JsonMapperUtil.readConnectionMessage(jsonMapper, str)
                    assert change.messageType == MessageType.CHANGE
                    assert change instanceof ChangeMessage
                    assert change.getId() != null
                    assert change.getType() == ObjectType.GAME_DIFF
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDiffDTO.class))
                            .expectNextMatches {
                                assert it.serverId != null
                                assert it.changedFields != null
                                assert it.changedFields.size() == 4
                                assert it.changedFields.contains("Name")
                                assert it.changedFields.contains("Version")
                                assert it.changedFields.contains("Categories")
                                assert it.changedFields.contains("Source")
                                assert it.name == modifiedViaDiffDTO.name
                                assert it.description == null
                                assert it.version == modifiedViaDiffDTO.version
                                def categories = it.categories
                                assert categories != null
                                assert categories.size() == 1
                                assert categories.get(0).id == newCategory.id
                                assert categories.get(0).name == newCategory.name
                                assert categories.get(0).createdAt == null
                                assert categories.get(0).createdBy == null
                                assert categories.get(0).modifiedAt == null
                                assert categories.get(0).modifiedBy == null
                                assert it.source?.id == newSecondSource.id
                                assert it.source?.name == newSecondSource.name
                                assert it.source?.createdAt == null
                                assert it.source?.createdBy == null
                                assert it.source?.modifiedAt == null
                                assert it.source?.modifiedBy == null
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
                    assert change.getType() == ObjectType.GAME
                    assert change.getClientId() == clientId
                    assert change.getObjectId() == collectedIds.first
                    assert !change.isForceFetch()
                    true
                }
                .then {
                    def getResponse = makeGetRequest(collectedIds.first)

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDTO.class))
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

    def "save game with images then remove images"() {
        given:
        GameDTO dto = GameFactoryUtil.createGame(UUID.randomUUID().toString(), "test")
        def icon = GameFactoryUtil.randomFile("Icon.png", 2048)
        def coverImage = GameFactoryUtil.randomFile("CoverImage.jpeg", 2048)
        def backgroundImage = GameFactoryUtil.randomFile("BackgroundImage.tif", 2048)
        def files = List.of(icon, coverImage, backgroundImage)
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def response = makeSaveRequest(dto, files)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, GameDTO.class))
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

        and:
        checkFiles(dto, { id ->
            assert metadataTestHelper.fileExists(ConstantsKt.GAME, id, "Icon.png")
            assert metadataTestHelper.fileExists(ConstantsKt.GAME, id, "CoverImage.jpeg")
            assert metadataTestHelper.fileExists(ConstantsKt.GAME, id, "BackgroundImage.tif")
        })

        when:
        GameDiffDTO diffDTO = new GameDiffDTO()
        diffDTO.setId(dto.getId())
        diffDTO.setGameId(dto.getGameId())
        diffDTO.setPluginId(dto.getPluginId())
        diffDTO.setChangedFields(List.of("Icon", "CoverImage", "BackgroundImage"))

        def diffResponse = makeSaveDiffRequest(diffDTO, List.of())

        then:
        diffResponse.expectStatus().is2xxSuccessful()

        and:
        checkFiles(dto, { id ->
            assert metadataTestHelper.fileDoesNotExist(ConstantsKt.GAME, id, "Icon.png")
            assert metadataTestHelper.fileDoesNotExist(ConstantsKt.GAME, id, "CoverImage.jpeg")
            assert metadataTestHelper.fileDoesNotExist(ConstantsKt.GAME, id, "BackgroundImage.tif")
        })
    }


    @Override
    protected String uri() {
        return "/api/game"
    }

    protected static String diffUri() {
        return "/api/game-diff"
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

    protected assertEntityAndGetResponse(GameDTO dto, List<MultipartFile> files, Long id) {
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

        for (final def file in files) {
            assert metadataTestHelper.fileExists(ConstantsKt.GAME, savedEntity.getId(), file.name)
        }

        true
    }

    private checkFiles(GameDTO dto, Consumer<Long> idChecker) {
        def savedEntities = repository().findByPlayniteId(dto.id).collectList().block()
        assert savedEntities.size() == 1

        def id = savedEntities[0].id
        idChecker.accept(id)
        true
    }

    protected WebTestClient.ResponseSpec makeGetMetadataRequest(Long id, String metadataName) {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/game-metadata/$id/$metadataName")
                        .build())
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeSaveDiffRequest(GameDiffDTO dto, List<MultipartFile> files) {
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
