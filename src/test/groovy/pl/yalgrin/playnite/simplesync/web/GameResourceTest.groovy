package pl.yalgrin.playnite.simplesync.web


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.domain.Game
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO
import pl.yalgrin.playnite.simplesync.dto.GameDTO
import pl.yalgrin.playnite.simplesync.enums.ObjectType
import pl.yalgrin.playnite.simplesync.repository.GameRepository
import pl.yalgrin.playnite.simplesync.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import reactor.test.StepVerifier

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicLong

class GameResourceTest extends AbstractObjectWithDiffTest<Game, GameDTO> {

    @Autowired
    private GameRepository gameRepository

    def "save single game"() {
        given:
        GameDTO dto = GameDTO.builder()
                .id(UUID.randomUUID().toString())
                .gameId(UUID.randomUUID().toString())
                .pluginId(UUID.randomUUID().toString())
                .name("test")
                .build()
        byte[] iconBytes = new byte[2048]
        byte[] coverImageBytes = new byte[4096]
        byte[] backgroundImageBytes = new byte[8192]

        ThreadLocalRandom.current().nextBytes(iconBytes)
        ThreadLocalRandom.current().nextBytes(coverImageBytes)
        ThreadLocalRandom.current().nextBytes(backgroundImageBytes)
        when:
        def response = makeSaveRequest(dto, List.of(new MockMultipartFile("Icon.png", "Icon.png", MediaType.APPLICATION_OCTET_STREAM_VALUE, iconBytes), new MockMultipartFile("CoverImage.jpeg", "CoverImage.jpeg", MediaType.APPLICATION_OCTET_STREAM_VALUE, coverImageBytes), new MockMultipartFile("BackgroundImage.tif", "BackgroundImage.tif", MediaType.APPLICATION_OCTET_STREAM_VALUE, backgroundImageBytes)))

        then:
        response.expectStatus().is2xxSuccessful()

        and:
        StepVerifier.create(IntegrationTestUtil.getReturnMono(response, GameDTO.class))
                .expectNextMatches { objectMatches(it, dto) }
                .verifyComplete()

        and:
        assertEntityAndGetResponse(dto)
    }

    def "save multiple games"() {
        given:
        List<GameDTO> list = new ArrayList<>()
        for (int i = 0; i < 1000; i++) {
            list.add(GameDTO.builder()
                    .id("id-" + i)
                    .gameId(UUID.randomUUID().toString())
                    .pluginId(UUID.randomUUID().toString())
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
            StepVerifier.create(IntegrationTestUtil.getReturnMono(tuple.getV1(), GameDTO.class))
                    .expectNextMatches { objectMatches(it, list.get(tuple.getV2())) }
                    .verifyComplete()
            true
        }

        and:
        list.stream().allMatch { dto -> assertEntityAndGetResponse(dto) }
    }

    def "save game and then delete it"() {
        given:
        GameDTO dto = GameDTO.builder()
                .id(UUID.randomUUID().toString())
                .gameId(UUID.randomUUID().toString())
                .pluginId(UUID.randomUUID().toString())
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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, GameDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save and then remove repeatedly"() {
        given:
        GameDTO dto = GameDTO.builder()
                .id(UUID.randomUUID().toString())
                .gameId(UUID.randomUUID().toString())
                .pluginId(UUID.randomUUID().toString())
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
        StepVerifier.create(IntegrationTestUtil.getReturnMono(deleteResponse, GameDTO.class))
                .verifyComplete()
        assertDeleted(dto)
    }

    def "save, modify and delete and await the change stream"() {
        given:
        GameDTO toSave = GameDTO.builder()
                .id(UUID.randomUUID().toString())
                .gameId(UUID.randomUUID().toString())
                .pluginId(UUID.randomUUID().toString())
                .name("new")
                .build()
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
                    makeSaveRequest(toSave).expectStatus().is2xxSuccessful()
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
        assert resultDTO.getId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }

    @Override
    boolean objectMatches(Game resultDTO, GameDTO expectedDTO) {
        assert resultDTO.getPlayniteId() == expectedDTO.getId()
        assert resultDTO.getName() == expectedDTO.getName()
        assert resultDTO.isRemoved() == expectedDTO.isRemoved()
        true
    }
}
