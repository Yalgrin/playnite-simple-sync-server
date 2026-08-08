package pl.yalgrin.playnite.simplesync.service.objects;

import io.vavr.Tuple2;
import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO;
import pl.yalgrin.playnite.simplesync.library.dto.LibraryObjectDTO;
import reactor.core.publisher.Mono;

public interface ObjectSaveService<DTO extends LibraryObjectDTO> {
    Mono<DTO> saveObject(DTO objectDTO);

    Mono<Tuple2<DTO, ChangeDTO>> saveObjectWithoutPublishing(DTO objectDTO, String clientId);
}
