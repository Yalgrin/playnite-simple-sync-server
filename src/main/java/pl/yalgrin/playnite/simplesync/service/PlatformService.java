package pl.yalgrin.playnite.simplesync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.config.Constants;
import pl.yalgrin.playnite.simplesync.domain.Platform;
import pl.yalgrin.playnite.simplesync.domain.PlatformDiff;
import pl.yalgrin.playnite.simplesync.dto.PlatformDTO;
import pl.yalgrin.playnite.simplesync.dto.PlatformDiffDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.PlatformMapper;
import pl.yalgrin.playnite.simplesync.repository.PlatformDiffRepository;
import pl.yalgrin.playnite.simplesync.repository.PlatformRepository;

import java.util.Set;

@Service
public class PlatformService extends
        AbstractObjectWithMetadataService<Platform, PlatformDiff, PlatformDTO, PlatformDiffDTO> {

    public PlatformService(PlatformRepository repository, PlatformDiffRepository diffRepository, PlatformMapper mapper,
                           ChangeService changeService, ChangeListenerService changeListenerService,
                           MetadataService metadataService, ObjectMapper objectMapper,
                           TransactionalOperator transactionalOperator) {
        super(repository, diffRepository, mapper, changeService, changeListenerService, metadataService, objectMapper,
                transactionalOperator);
    }

    @Override
    protected Set<String> getMetadataFields() {
        return Set.of(Constants.ICON, Constants.COVER_IMAGE, Constants.BACKGROUND_IMAGE);
    }

    @Override
    protected void setHex(Platform platform, String baseName, String md5) {
        if (Constants.ICON.equals(baseName)) {
            platform.setIconMd5(md5);
            platform.setChanged(true);
        } else if (Constants.COVER_IMAGE.equals(baseName)) {
            platform.setCoverImageMd5(md5);
            platform.setChanged(true);
        } else if (Constants.BACKGROUND_IMAGE.equals(baseName)) {
            platform.setBackgroundImageMd5(md5);
            platform.setChanged(true);
        }
    }

    @Override
    protected boolean shouldSaveMetadata(Platform platform, byte[] bytes, String md5, String basename) {
        if (platform.getId() == null) {
            return true;
        }
        String md5ToCompare = null;
        if (Constants.ICON.equals(basename)) {
            md5ToCompare = platform.getIconMd5();
        } else if (Constants.COVER_IMAGE.equals(basename)) {
            md5ToCompare = platform.getCoverImageMd5();
        } else if (Constants.BACKGROUND_IMAGE.equals(basename)) {
            md5ToCompare = platform.getBackgroundImageMd5();
        }
        return md5ToCompare == null || !StringUtils.equals(md5ToCompare, md5);
    }

    @Override
    protected String getMetadataFolder() {
        return Constants.PLATFORM;
    }

    @Override
    protected Platform createEntityFromDTO(PlatformDTO dto) {
        return Platform.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Platform;
    }

    @Override
    protected ObjectType getDiffType() {
        return ObjectType.PlatformDiff;
    }

    @Override
    protected Class<PlatformDiffDTO> getDiffDtoClass() {
        return PlatformDiffDTO.class;
    }
}
