package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.Source;
import pl.yalgrin.playnite.simplesync.dto.SourceDTO;

@Component
public class SourceMapper extends AbstractObjectMapper<Source, SourceDTO> {

    @Override
    protected SourceDTO createDTO() {
        return new SourceDTO();
    }
}
