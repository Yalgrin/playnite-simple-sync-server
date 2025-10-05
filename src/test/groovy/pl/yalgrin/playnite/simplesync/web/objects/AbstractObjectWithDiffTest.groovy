package pl.yalgrin.playnite.simplesync.web.objects

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.multipart.MultipartFile
import pl.yalgrin.playnite.simplesync.domain.objects.AbstractObjectEntity
import pl.yalgrin.playnite.simplesync.dto.objects.AbstractObjectDTO

abstract class AbstractObjectWithDiffTest<E extends AbstractObjectEntity, D extends AbstractObjectDTO> extends AbstractObjectTest<E, D> {

    @Override
    protected WebTestClient.ResponseSpec makeSaveRequest(D dto) {
        return makeSaveRequest(dto, List.of())
    }

    protected WebTestClient.ResponseSpec makeSaveRequest(D dto, List<MultipartFile> files) {
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
                        .path("${uri()}/save")
                        .queryParam("clientId", "test")
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
    }

}
