package pl.yalgrin.playnite.simplesync.service;

import io.vavr.Tuple2;
import pl.yalgrin.playnite.simplesync.dto.AbstractObjectDTO;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import reactor.core.publisher.Mono;

public interface ObjectSaveService<DTO extends AbstractObjectDTO> {
    Mono<DTO> saveObject(DTO objectDTO, String clientId);

    Mono<Tuple2<DTO, ChangeDTO>> saveObjectWithoutPublishing(DTO objectDTO, String clientId);
}
