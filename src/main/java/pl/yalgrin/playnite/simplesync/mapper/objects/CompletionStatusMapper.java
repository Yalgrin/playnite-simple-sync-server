package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.library.domain.CompletionStatus;
import pl.yalgrin.playnite.simplesync.library.dto.CompletionStatusDTO;

@Component
public class CompletionStatusMapper extends AbstractObjectMapper<CompletionStatus, CompletionStatusDTO> {

    @Override
    protected CompletionStatusDTO createDTO() {
        return new CompletionStatusDTO();
    }
}
