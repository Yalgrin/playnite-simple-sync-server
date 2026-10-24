package pl.yalgrin.playnite.simplesync.web.panel

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
import pl.yalgrin.playnite.simplesync.library.dto.GameDiffDTO
import pl.yalgrin.playnite.simplesync.library.repository.GameRepository
import pl.yalgrin.playnite.simplesync.library.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.panel.dto.PanelCategoryDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGameDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelGenreDTO
import pl.yalgrin.playnite.simplesync.panel.dto.PanelSourceDTO
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import pl.yalgrin.playnite.simplesync.util.JsonMapperUtil
import pl.yalgrin.playnite.simplesync.util.panel.PanelGameAssertionUtil
import pl.yalgrin.playnite.simplesync.util.panel.PanelGameFactoryUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class PanelGameResourceTest extends AbstractPanelObjectWithDiffTest<Game, PanelGameDTO> {

    @Autowired
    private GameRepository gameRepository
    @Autowired
    private MetadataTestHelper metadataTestHelper

    def "save single game"() {
        given:
        PanelGameDTO dto = PanelGameFactoryUtil.createGame("test")
        AtomicLong newObjectId = new AtomicLong(-1)
        def icon = PanelGameFactoryUtil.randomFile("Icon.png", 2048)
        def coverImage = PanelGameFactoryUtil.randomFile("CoverImage.jpeg", 2048)
        def backgroundImage = PanelGameFactoryUtil.randomFile("BackgroundImage.tif", 2048)
        def files = List.of(icon, coverImage, backgroundImage)

        when:
        def response = makeSaveRequest(dto, files)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, PanelGameDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId.set(it.id)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, files, newObjectId.get())

        and:
        assert metadataTestHelper.fileExists(ConstantsKt.GAME, newObjectId.get(), "Icon.png")
        assert metadataTestHelper.fileExists(ConstantsKt.GAME, newObjectId.get(), "CoverImage.jpeg")
        assert metadataTestHelper.fileExists(ConstantsKt.GAME, newObjectId.get(), "BackgroundImage.tif")
    }

    def "save multiple games"() {
        given:
        List<Tuple2<PanelGameDTO, List<MultipartFile>>> list = new ArrayList<>()
        for (int i = 0; i < 100; i++) {
            def game = PanelGameFactoryUtil.gameWithIndex(i)
            def files = PanelGameFactoryUtil.randomFiles()
            game.setSavingIcon(files.any { it.name.startsWith("Icon") })
            game.setSavingCoverImage(files.any { it.name.startsWith("CoverImage") })
            game.setSavingBackground(files.any { it.name.startsWith("BackgroundImage") })
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
            StepVerifier.create(IntegrationTestUtil.getReturnMono(tuple.getV1(), PanelGameDTO.class))
                    .expectNextMatches {
                        assert it.id != null
                        createdIds.add(it.id)
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
        PanelGameDTO dto = PanelGameFactoryUtil.randomGame()
        def files = PanelGameFactoryUtil.randomFiles()
        dto.setSavingIcon(files.any { it.name.startsWith("Icon") })
        dto.setSavingCoverImage(files.any { it.name.startsWith("CoverImage") })
        dto.setSavingBackground(files.any { it.name.startsWith("BackgroundImage") })
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, PanelGameDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId.set(it.id)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, files, newObjectId.get())

        when:
        dto.id = newObjectId.get()
        def deleteResponse = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, PanelGameDTO.class))
                .verifyComplete()
        assertDeleted(newObjectId.get())
    }

    def "save and then remove repeatedly"() {
        given:
        PanelGameDTO dto = PanelGameFactoryUtil.randomGame()
        def files = PanelGameFactoryUtil.randomFiles()
        dto.setSavingIcon(files.any { it.name.startsWith("Icon") })
        dto.setSavingCoverImage(files.any { it.name.startsWith("CoverImage") })
        dto.setSavingBackground(files.any { it.name.startsWith("BackgroundImage") })
        AtomicLong newObjectId = new AtomicLong(-1)

        when:
        def saveResponse = makeSaveRequest(dto, files)

        then:
        saveResponse.expectStatus().is2xxSuccessful()
        StepVerifier.create(IntegrationTestUtil.getReturnMono(saveResponse, PanelGameDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    newObjectId.set(it.id)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto, files, newObjectId.get())

        when:
        dto.id = newObjectId.get()
        def deleteResponse = makeDeleteRequest(dto)
        def deleteResponse2 = makeDeleteRequest(dto)

        then:
        deleteResponse.expectStatus().is2xxSuccessful()
        deleteResponse2.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, PanelGameDTO.class))
                .verifyComplete()
        assertDeleted(newObjectId.get())
    }

    def "save, modify twice and delete and await the change stream"() {
        given:
        PanelGameDTO toSave = PanelGameFactoryUtil.randomGame()
        def files = PanelGameFactoryUtil.randomFiles(100, 100, 100)
        toSave.setSavingIcon(files.any { it.name.startsWith("Icon") })
        toSave.setSavingCoverImage(files.any { it.name.startsWith("CoverImage") })
        toSave.setSavingBackground(files.any { it.name.startsWith("BackgroundImage") })

        when:
        def changeRequest = makeConnectRequest(otherClientInfo)
        def responseFlux = changeRequest.returnResult(new ParameterizedTypeReference<String>() {}).responseBody

        then:
        List<Long> collectedIds = Collections.synchronizedList(new ArrayList<>())
        AtomicLong newDiffObjectId = new AtomicLong(-1)
        AtomicLong newSecondDiffObjectId = new AtomicLong(-1)
        AtomicReference<String> sessionId = new AtomicReference<>()
        AtomicReference<PanelGameDTO> savedFirstGame = new AtomicReference<>()
        AtomicReference<PanelGameDTO> modifiedGame = new AtomicReference<>()
        AtomicReference<PanelGameDTO> savedModifiedGame = new AtomicReference<>()
        AtomicReference<PanelGameDTO> secondModifiedGame = new AtomicReference<>()
        AtomicReference<PanelGameDTO> secondSavedModifiedGame = new AtomicReference<>()
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
                            .expectBody(PanelGameDTO.class)
                            .returnResult()
                            .responseBody
                    PanelGameAssertionUtil.assertGame(toSave, result)
                    savedFirstGame.set(result)
                    assert result.id != null
                    collectedIds.add(result.id)
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PanelGameDTO.class))
                            .expectNextMatches {
                                assert it.id == collectedIds.first
                                objectMatches(it, toSave)
                            }
                            .verifyComplete()
                }
                .then {
                    def modified = savedFirstGame.get().withName("some other name")
                    modified.setDescription("Description")
                    modified.setSavingIcon(false)
                    modified.setSavingCoverImage(false)
                    modified.setSavingBackground(false)
                    def newGenre = new PanelGenreDTO()
                    newGenre.name = "New genre"
                    modified.genres = List.of(newGenre)

                    def newSource = new PanelSourceDTO()
                    newSource.name = "New source"
                    modified.source = newSource
                    modifiedGame.set(modified)

                    def result = makeSaveRequest(modified, files).expectStatus().is2xxSuccessful()
                            .expectBody(PanelGameDTO.class)
                            .returnResult()
                            .responseBody
                    PanelGameAssertionUtil.assertGame(modified, result)
                    savedModifiedGame.set(result)
                    assert result.id == collectedIds.first
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

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, PanelGameDTO.class))
                            .expectNextMatches {
                                assert it.id == collectedIds.first
                                objectMatches(it, modifiedGame.get())
                            }
                            .verifyComplete()
                }
                .then {
                    assert newDiffObjectId.get() != -1
                    def getResponse = makeGetDiffRequest(newDiffObjectId.get(), sessionId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDiffDTO.class))
                            .expectNextMatches {
                                assert it.serverId != null
                                assert it.id != null
                                assert it.baseObjectId == collectedIds.first
                                assert it.gameId == savedFirstGame.get().gameId
                                assert it.pluginId != null
                                assert it.pluginName == toSave.plugin.name
                                assert it.changedFields != null
                                assert it.changedFields.size() == 4
                                assert it.changedFields.contains("Name")
                                assert it.changedFields.contains("Description")
                                assert it.changedFields.contains("Genres")
                                assert it.changedFields.contains("Source")
                                assert it.name == modifiedGame.get().name
                                assert it.description == modifiedGame.get().description
                                assert it.pluginName == modifiedGame.get().plugin.name
                                assert it.version == null
                                def genres = it.genres
                                assert genres != null
                                assert genres.size() == 1
                                assert genres.get(0).serverId == savedModifiedGame.get().genres.first.id
                                assert genres.get(0).name == modifiedGame.get().genres.first.name
                                assert genres.get(0).createdAt == null
                                assert genres.get(0).createdBy == null
                                assert genres.get(0).modifiedAt == null
                                assert genres.get(0).modifiedBy == null
                                assert it.source?.serverId == savedModifiedGame.get().source.id
                                assert it.source?.name == modifiedGame.get().source.name
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
                    PanelGameDTO modifiedAgain = savedModifiedGame.get().withName("even different name")
                    modifiedAgain.version = "3.0"
                    def newCategory = new PanelCategoryDTO()
                    newCategory.name = "New category"
                    modifiedAgain.categories = List.of(newCategory)
                    def newSecondSource = new PanelSourceDTO()
                    newSecondSource.name = "New source 2"
                    modifiedAgain.source = newSecondSource
                    secondModifiedGame.set(modifiedAgain)

                    def result = makeSaveRequest(modifiedAgain, List.of()).expectStatus().is2xxSuccessful()
                            .expectBody(PanelGameDTO.class)
                            .returnResult()
                            .responseBody
                    secondSavedModifiedGame.set(result)
                    assert result.name == modifiedAgain.name
                    assert result.description == modifiedGame.get().description
                    assert result.version == modifiedAgain.version
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
                    def getResponse = makeGetDiffRequest(newSecondDiffObjectId.get(), sessionId.get())

                    getResponse.expectStatus().is2xxSuccessful()

                    StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, GameDiffDTO.class))
                            .expectNextMatches {
                                assert it.serverId != null
                                assert it.id != null
                                assert it.baseObjectId == collectedIds.first
                                assert it.gameId == savedFirstGame.get().gameId
                                assert it.pluginId != null
                                assert it.pluginName == toSave.plugin.name
                                assert it.changedFields != null
                                assert it.changedFields.size() == 4
                                assert it.changedFields.contains("Name")
                                assert it.changedFields.contains("Version")
                                assert it.changedFields.contains("Categories")
                                assert it.changedFields.contains("Source")
                                assert it.name == secondModifiedGame.get().name
                                assert it.description == null
                                assert it.version == secondModifiedGame.get().version
                                def categories = it.categories
                                assert categories != null
                                assert categories.size() == 1
                                assert categories.get(0).serverId == secondSavedModifiedGame.get().categories.first.id
                                assert categories.get(0).name == secondModifiedGame.get().categories.first.name
                                assert categories.get(0).createdAt == null
                                assert categories.get(0).createdBy == null
                                assert categories.get(0).modifiedAt == null
                                assert categories.get(0).modifiedBy == null
                                assert it.source?.serverId == secondSavedModifiedGame.get().source.id
                                assert it.source?.name == secondModifiedGame.get().source.name
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
                    makeDeleteRequest(secondSavedModifiedGame.get()).expectStatus().is2xxSuccessful()
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

                    getResponse.expectStatus().is4xxClientError()
                }
                .thenCancel()
                .verify()
    }

    def "save game with images then remove images"() {
        given:
        PanelGameDTO dto = PanelGameFactoryUtil.createGame("test")
        def icon = PanelGameFactoryUtil.randomFile("Icon.png", 2048)
        def coverImage = PanelGameFactoryUtil.randomFile("CoverImage.jpeg", 2048)
        def backgroundImage = PanelGameFactoryUtil.randomFile("BackgroundImage.tif", 2048)
        def files = List.of(icon, coverImage, backgroundImage)
        dto.savingIcon = true
        dto.savingCoverImage = true
        dto.savingBackground = true
        AtomicReference<PanelGameDTO> savedFirstGame = new AtomicReference<>()

        when:
        def response = makeSaveRequest(dto, files)

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, PanelGameDTO.class))
                .expectNextMatches {
                    assert it.id != null
                    savedFirstGame.set(it)
                    objectMatches(it, dto)
                }
                .verifyComplete()

        and:
        def id = savedFirstGame.get().id
        assertEntityAndGetResponse(dto, files, id)

        and:
        assert metadataTestHelper.fileExists(ConstantsKt.GAME, id, "Icon.png")
        assert metadataTestHelper.fileExists(ConstantsKt.GAME, id, "CoverImage.jpeg")
        assert metadataTestHelper.fileExists(ConstantsKt.GAME, id, "BackgroundImage.tif")

        when:
        PanelGameDTO diffDTO = savedFirstGame.get().copyAll()
        diffDTO.savingIcon = true
        diffDTO.savingCoverImage = true
        diffDTO.savingBackground = true

        def diffResponse = makeSaveRequest(diffDTO, List.of())

        then:
        diffResponse.expectStatus().is2xxSuccessful()

        and:
        assert metadataTestHelper.fileDoesNotExist(ConstantsKt.GAME, id, "Icon.png")
        assert metadataTestHelper.fileDoesNotExist(ConstantsKt.GAME, id, "CoverImage.jpeg")
        assert metadataTestHelper.fileDoesNotExist(ConstantsKt.GAME, id, "BackgroundImage.tif")
    }


    @Override
    protected String uri() {
        return "/api/panel/game"
    }

    protected static String diffUri() {
        return "/api/game-diff"
    }


    @Override
    protected ObjectRepository<Game> repository() {
        return gameRepository
    }

    @Override
    protected Class<? extends PanelGameDTO> dtoClass() {
        return PanelGameDTO.class
    }

    @Override
    boolean objectMatches(PanelGameDTO resultDTO, PanelGameDTO expectedDTO) {
        PanelGameAssertionUtil.assertGame(expectedDTO, resultDTO)
    }

    @Override
    boolean objectMatches(Game resultDTO, PanelGameDTO expectedDTO) {
        PanelGameAssertionUtil.assertGameEntity(expectedDTO, resultDTO)
    }

    protected assertEntityAndGetResponse(PanelGameDTO dto, List<MultipartFile> files, Long id) {
        def savedEntity = repository().findById(id).block()
        assert savedEntity != null

        assert objectMatches(savedEntity, dto)

        def getResponse = makeGetRequest(savedEntity.getId())

        getResponse.expectStatus().is2xxSuccessful()

        StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, dtoClass()))
                .expectNextMatches {
                    assert it.id == id
                    objectMatches(it, dto)
                }
                .verifyComplete()

        Map<String, MultipartFile> expectedFileMap = new HashMap<>()
        for (final def file in files) {
            expectedFileMap.put(FilenameUtils.getBaseName(file.getName()), file)
        }
        for (final def filename in ConstantsKt.ALLOWED_FILE_NAMES) {
            def expectedFile = expectedFileMap.get(filename)
            def metadataRequest = makeGetMetadataRequest(savedEntity.getId(), filename, getMd5(savedEntity, filename))
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

    private String getMd5(Game game, String filename) {
        if (filename == ConstantsKt.ICON) {
            return game.iconMd5
        } else if (filename == ConstantsKt.COVER_IMAGE) {
            return game.coverImageMd5
        } else if (filename == ConstantsKt.BACKGROUND_IMAGE) {
            return game.backgroundImageMd5
        } else {
            throw IllegalArgumentException("Invalid metadata $filename")
        }
    }

    private checkFiles(PanelGameDTO dto, Consumer<Long> idChecker) {
        idChecker.accept(dto.id)
        true
    }

    protected WebTestClient.ResponseSpec makeGetMetadataRequest(Long id, String metadataName, String md5) {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/panel/game/$id/metadata/$metadataName/$md5")
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

    protected WebTestClient.ResponseSpec makeGetDiffRequest(Long id, String sessionId) {
        rawWebTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("${diffUri()}/$id")
                        .build())
                .header("X-Client-Id", otherClientInfo.clientId)
                .header("X-Client-Token", otherClientInfo.clientToken)
                .header("X-Session-Id", sessionId)
                .exchange()
    }
}
