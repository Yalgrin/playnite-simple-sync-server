package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.dto.objects.SourceDTO;
import pl.yalgrin.playnite.simplesync.library.domain.Source;

@Component
public class SourceMapper extends AbstractObjectMapper<Source, SourceDTO> {

    @Override
    protected SourceDTO createDTO() {
        return new SourceDTO();
    }
}
