package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.CompletionStatus;
import pl.yalgrin.playnite.simplesync.dto.CompletionStatusDTO;

@Component
public class CompletionStatusMapper extends AbstractObjectMapper<CompletionStatus, CompletionStatusDTO> {

    @Override
    protected CompletionStatusDTO createDTO() {
        return new CompletionStatusDTO();
    }
}
