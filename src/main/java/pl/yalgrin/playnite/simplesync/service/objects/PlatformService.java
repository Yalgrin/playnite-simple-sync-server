package pl.yalgrin.playnite.simplesync.service.objects;

import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.change.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.change.service.ChangeService;
import pl.yalgrin.playnite.simplesync.common.config.ConstantsKt;
import pl.yalgrin.playnite.simplesync.common.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.library.domain.Platform;
import pl.yalgrin.playnite.simplesync.library.domain.PlatformDiff;
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDTO;
import pl.yalgrin.playnite.simplesync.library.dto.PlatformDiffDTO;
import pl.yalgrin.playnite.simplesync.library.repository.PlatformDiffRepository;
import pl.yalgrin.playnite.simplesync.library.repository.PlatformRepository;
import pl.yalgrin.playnite.simplesync.mapper.objects.PlatformMapper;
import pl.yalgrin.playnite.simplesync.service.MetadataService;
import tools.jackson.databind.ObjectMapper;

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
        return Set.of(ConstantsKt.ICON, ConstantsKt.COVER_IMAGE, ConstantsKt.BACKGROUND_IMAGE);
    }

    @Override
    protected void setHex(Platform platform, String baseName, String md5) {
        if (ConstantsKt.ICON.equals(baseName)) {
            platform.setIconMd5(md5);
            platform.setChanged(true);
        } else if (ConstantsKt.COVER_IMAGE.equals(baseName)) {
            platform.setCoverImageMd5(md5);
            platform.setChanged(true);
        } else if (ConstantsKt.BACKGROUND_IMAGE.equals(baseName)) {
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
        if (ConstantsKt.ICON.equals(basename)) {
            md5ToCompare = platform.getIconMd5();
        } else if (ConstantsKt.COVER_IMAGE.equals(basename)) {
            md5ToCompare = platform.getCoverImageMd5();
        } else if (ConstantsKt.BACKGROUND_IMAGE.equals(basename)) {
            md5ToCompare = platform.getBackgroundImageMd5();
        }
        return md5ToCompare == null || !Strings.CS.equals(md5ToCompare, md5);
    }

    @Override
    protected String getMetadataFolder() {
        return ConstantsKt.PLATFORM;
    }

    @Override
    protected Platform createEntityFromDTO(PlatformDTO dto) {
        Platform platform = new Platform();
        platform.setPlayniteId(dto.getId());
        return platform;
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.PLATFORM;
    }

    @Override
    protected ObjectType getDiffType() {
        return ObjectType.PLATFORM_DIFF;
    }

    @Override
    protected Class<PlatformDiffDTO> getDiffDtoClass() {
        return PlatformDiffDTO.class;
    }
}
