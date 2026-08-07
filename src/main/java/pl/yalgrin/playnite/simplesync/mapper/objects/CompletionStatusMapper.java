package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.dto.objects.CompletionStatusDTO;
import pl.yalgrin.playnite.simplesync.library.domain.CompletionStatus;

@Component
public class CompletionStatusMapper extends AbstractObjectMapper<CompletionStatus, CompletionStatusDTO> {

    @Override
    protected CompletionStatusDTO createDTO() {
        return new CompletionStatusDTO();
    }
}
