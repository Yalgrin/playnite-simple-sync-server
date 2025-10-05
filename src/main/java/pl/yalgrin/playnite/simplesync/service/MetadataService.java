package pl.yalgrin.playnite.simplesync.service;

import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import pl.yalgrin.playnite.simplesync.config.Constants;
import pl.yalgrin.playnite.simplesync.repository.GameRepository;
import pl.yalgrin.playnite.simplesync.repository.PlatformRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@DependsOn("liquibase")
@Slf4j
@RequiredArgsConstructor
public class MetadataService {

    private static final Set<String> ALLOWED_FOLDERS = Set.of(Constants.PLATFORM, Constants.GAME);
    public static final Set<String> ALLOWED_FILE_NAMES = Set.of(Constants.ICON, Constants.COVER_IMAGE,
            Constants.BACKGROUND_IMAGE);

    @Value("${application.metadata-folder}")
    private String metadataFolder;

    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;

    @PostConstruct
    public void init() throws IOException {
        if (StringUtils.isBlank(metadataFolder)) {
            throw new IllegalStateException("No metadata folder set!");
        }

        Path metadataPath = Path.of(metadataFolder);
        if (Files.exists(metadataPath)) {
            Files.list(metadataPath).forEach(this::checkFolderAndRemoveInvalidFolders);
        }
    }

    @SneakyThrows
    private void checkFolderAndRemoveInvalidFolders(Path path) {
        if (!ALLOWED_FOLDERS.contains(path.getFileName().toString())) {
            log.debug("checkFolderAndRemoveInvalidFolders > deleting main folder due to unsupported type: {}", path);
            deleteDirectory(path);
        } else if (path.getFileName().toString().equals(Constants.GAME)) {
            Files.list(path).forEach(gamePath -> {
                Long gameId = Try.of(() -> Long.parseLong(gamePath.getFileName().toString())).getOrNull();
                if (gameId != null) {
                    Boolean exists = gameRepository.existsById(gameId).block();
                    if (!BooleanUtils.isTrue(exists)) {
                        log.debug(
                                "checkFolderAndRemoveInvalidFolders > deleting game folder due to missing game with id: {}",
                                gamePath);
                        deleteDirectory(gamePath);
                    }
                } else {
                    log.debug("checkFolderAndRemoveInvalidFolders > deleting game folder due to invalid game id: {}",
                            gamePath);
                    deleteDirectory(gamePath);
                }
            });
        } else if (path.getFileName().toString().equals(Constants.PLATFORM)) {
            Files.list(path).forEach(gamePath -> {
                Long platformId = Try.of(() -> Long.parseLong(gamePath.getFileName().toString())).getOrNull();
                if (platformId != null) {
                    Boolean exists = platformRepository.existsById(platformId).block();
                    if (!BooleanUtils.isTrue(exists)) {
                        log.debug(
                                "checkFolderAndRemoveInvalidFolders > deleting platform folder due to missing platform with id: {}",
                                gamePath);
                        deleteDirectory(gamePath);
                    }
                } else {
                    log.debug(
                            "checkFolderAndRemoveInvalidFolders > deleting platform folder due to invalid platform id: {}",
                            gamePath);
                    deleteDirectory(gamePath);
                }
            });
        }
    }

    @SneakyThrows
    private static void deleteDirectory(Path path) {
        FileUtils.deleteDirectory(new File(path.toUri()));
    }

    public Mono<Boolean> saveMetadata(String folder, String idPart, String filename, byte[] bytes, String fieldName) {
        return Mono.defer(() -> {
            log.debug("saveMetadata > START, folder: {}, idPart: {}, filename: {}, fieldName: {}", folder, idPart,
                    filename, fieldName);

            if (StringUtils.isBlank(metadataFolder)) {
                log.debug("saveMetadata > ERROR, metadata folder is not specified!");
                return Mono.error(new IllegalStateException("No metadata folder set!"));
            }
            if (!ALLOWED_FILE_NAMES.contains(fieldName)) {
                log.debug("saveMetadata > ERROR, invalid field name: {}!", fieldName);
                return Mono.error(new IllegalArgumentException("Invalid file name!"));
            }
            Path dir = Path.of(metadataFolder, folder, idPart);
            String dirStr = dir.toString();
            return Mono.fromCallable(() -> {
                Files.createDirectories(dir);
                Files.list(dir).forEach(path -> {
                    String existingFileExtension = FilenameUtils.getExtension(path.toString());
                    String baseName = FilenameUtils.getBaseName(path.toString());
                    if (existingFileExtension.equalsIgnoreCase("tmp") || (baseName != null && baseName.startsWith(
                            fieldName + "."))) {
                        deleteIfExists(path);
                    }
                });
                Path path = Path.of(dirStr, filename);
                deleteIfExists(path);
                log.debug("saveMetadata > Writing file: {}", path);
                Files.write(path, bytes);
                return true;
            });
        }).doOnSuccess(d -> log.debug("saveMetadata > END"));
    }

    public Mono<Boolean> deleteMetadata(String folder, String idPart, String fieldName) {
        return Mono.defer(() -> {
            log.debug("deleteMetadata > START, folder: {}, idPart: {}, fieldName: {}", folder, idPart, fieldName);

            if (StringUtils.isBlank(metadataFolder)) {
                log.debug("deleteMetadata > ERROR, metadata folder is not specified!");
                return Mono.error(new IllegalStateException("No metadata folder set!"));
            }
            if (!ALLOWED_FILE_NAMES.contains(fieldName)) {
                log.debug("deleteMetadata > ERROR, invalid field name: {}!", fieldName);
                return Mono.error(new IllegalArgumentException("Invalid file name!"));
            }
            Path dir = Path.of(metadataFolder, folder, idPart);
            return Mono.fromCallable(() -> {
                if (Files.exists(dir)) {
                    AtomicBoolean deletedAnything = new AtomicBoolean(false);
                    Files.list(dir).forEach(path -> {
                        String existingFileExtension = FilenameUtils.getExtension(path.toString());
                        String baseName = FilenameUtils.getBaseName(path.toString());
                        if (existingFileExtension.equalsIgnoreCase("tmp") || (baseName != null && baseName.equals(
                                fieldName))) {
                            boolean result = deleteIfExists(path);
                            if (result) {
                                deletedAnything.set(true);
                            }
                        }
                    });
                    if (deletedAnything.get()) {
                        return true;
                    }
                }
                return null;
            });
        }).doOnSuccess(d -> log.debug("deleteMetadata > END"));
    }


    @SneakyThrows
    private static boolean deleteIfExists(Path path) {
        log.debug("Deleting file: {}", path);
        return Files.deleteIfExists(path);
    }

    public Mono<Tuple2<String, Flux<DataBuffer>>> getMetadata(String folder, String id, String filename) {
        return Mono.fromCallable(() -> {
            Path directory = Path.of(metadataFolder, folder, id);
            return Files.list(directory).filter(path -> {
                String baseName = FilenameUtils.getBaseName(path.toString());
                return baseName != null && StringUtils.equals(baseName, filename);
            }).findFirst().orElse(null);
        }).filter(Objects::nonNull).flatMap(path -> Mono.justOrEmpty(path).map(Path::getFileName).map(Path::toString)
                .zipWith(Mono.fromCallable(() -> DataBufferUtils.read(path, new DefaultDataBufferFactory(), 4096))));
    }
}
