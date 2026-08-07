package pl.yalgrin.playnite.simplesync.mapper.objects;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.dto.objects.TagDTO;
import pl.yalgrin.playnite.simplesync.library.domain.Tag;

@Component
public class TagMapper extends AbstractObjectMapper<Tag, TagDTO> {

    @Override
    protected TagDTO createDTO() {
        return new TagDTO();
    }
}
