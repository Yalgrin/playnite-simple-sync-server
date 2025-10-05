package pl.yalgrin.playnite.simplesync.service.objects;

import io.vavr.Tuple2;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.AbstractObjectDTO;
import reactor.core.publisher.Mono;

public interface ObjectSaveService<DTO extends AbstractObjectDTO> {
    Mono<DTO> saveObject(DTO objectDTO, String clientId);

    Mono<Tuple2<DTO, ChangeDTO>> saveObjectWithoutPublishing(DTO objectDTO, String clientId);
}
