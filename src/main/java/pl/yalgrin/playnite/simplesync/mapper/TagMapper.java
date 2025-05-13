package pl.yalgrin.playnite.simplesync.mapper;

import org.springframework.stereotype.Component;
import pl.yalgrin.playnite.simplesync.domain.Tag;
import pl.yalgrin.playnite.simplesync.dto.TagDTO;

@Component
public class TagMapper extends AbstractObjectMapper<Tag, TagDTO> {

    @Override
    protected TagDTO createDTO() {
        return new TagDTO();
    }
}
