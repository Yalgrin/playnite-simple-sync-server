package pl.yalgrin.playnite.simplesync.web

import org.springframework.test.web.reactive.server.WebTestClient
import pl.yalgrin.playnite.simplesync.SpockIntegrationTest
import pl.yalgrin.playnite.simplesync.domain.AbstractObjectEntity
import pl.yalgrin.playnite.simplesync.dto.AbstractObjectDTO
import pl.yalgrin.playnite.simplesync.dto.AgeRatingDTO
import pl.yalgrin.playnite.simplesync.repository.ObjectRepository
import pl.yalgrin.playnite.simplesync.util.IntegrationTestUtil
import reactor.test.StepVerifier

abstract class AbstractObjectTest<E extends AbstractObjectEntity, D extends AbstractObjectDTO> extends SpockIntegrationTest {
    protected WebTestClient.ResponseSpec makeGetRequest(Long id) {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri()}/$id")
                        .build())
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeSaveRequest(D dto) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri()}/save")
                        .queryParam("clientId", "test")
                        .build())
                .bodyValue(dto)
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeDeleteRequest(D dto) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("${uri()}/delete")
                        .queryParam("clientId", "test")
                        .build())
                .bodyValue(dto)
                .exchange()
    }

    protected WebTestClient.ResponseSpec makeChangeStreamRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/change/stream")
                        .queryParam("clientId", "test")
                        .build())
                .exchange()
    }

    protected assertEntityAndGetResponse(D dto) {
        def savedEntities = repository().findByPlayniteId(dto.id).collectList().block()
        assert savedEntities.size() == 1

        def savedEntity = savedEntities[0]
        assert objectMatches(savedEntity, dto)

        def getResponse = makeGetRequest(savedEntity.getId())

        getResponse.expectStatus().is2xxSuccessful()

        StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, dtoClass()))
                .expectNextMatches { objectMatches(it, dto) }
                .verifyComplete()
        true
    }

    boolean assertDeleted(D dto) {
        def savedEntities = repository().findByPlayniteId(dto.id).collectList().block()
        if (savedEntities.isEmpty()) {
            true
        } else {
            assert savedEntities.size() == 1

            def savedEntity = savedEntities[0]
            assert savedEntity.isRemoved()

            def getResponse = makeGetRequest(savedEntity.getId())

            getResponse.expectStatus().is2xxSuccessful()

            StepVerifier.create(IntegrationTestUtil.getReturnMono(getResponse, AgeRatingDTO.class))
                    .expectNextMatches {
                        assert it.isRemoved()
                        true
                    }
                    .verifyComplete()
            true
        }
    }

    protected abstract String uri();

    protected abstract ObjectRepository<E> repository();

    protected abstract Class<? extends D> dtoClass();

    protected abstract boolean objectMatches(D resultDTO, D expectedDTO)

    protected abstract boolean objectMatches(E resultDTO, D expectedDTO)
}
