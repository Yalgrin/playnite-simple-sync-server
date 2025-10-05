package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.objects.CompletionStatus;
import pl.yalgrin.playnite.simplesync.dto.objects.CompletionStatusDTO;

@Component
public class CompletionStatusMapper extends AbstractObjectMapper<CompletionStatus, CompletionStatusDTO> {

    @Override
    protected CompletionStatusDTO createDTO() {
        return new CompletionStatusDTO();
    }
}
