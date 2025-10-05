package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.objects.Source;
import pl.yalgrin.playnite.simplesync.dto.objects.SourceDTO;

@Component
public class SourceMapper extends AbstractObjectMapper<Source, SourceDTO> {

    @Override
    protected SourceDTO createDTO() {
        return new SourceDTO();
    }
}
