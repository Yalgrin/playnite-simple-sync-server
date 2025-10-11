package pl.yalgrin.playnite.simplesync.service;

import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import pl.yalgrin.playnite.simplesync.config.Constants;
import pl.yalgrin.playnite.simplesync.repository.objects.GameRepository;
import pl.yalgrin.playnite.simplesync.repository.objects.PlatformRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@DependsOnDatabaseInitialization
@Slf4j
@RequiredArgsConstructor
public class MetadataService {

    private static final Set<String> ALLOWED_FOLDERS = Set.of(Constants.PLATFORM, Constants.GAME);
    public static final Set<String> ALLOWED_FILE_NAMES = Set.of(Constants.ICON, Constants.COVER_IMAGE,
            Constants.BACKGROUND_IMAGE);
    public static final int PARTITION_SIZE = 990;

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
            try (Stream<Path> files = Files.list(metadataPath)) {
                files.forEach(this::checkFolderAndRemoveInvalidFolders);
            }
        }
    }

    @SneakyThrows
    private void checkFolderAndRemoveInvalidFolders(Path path) {
        if (!ALLOWED_FOLDERS.contains(path.getFileName().toString())) {
            log.debug("checkFolderAndRemoveInvalidFolders > deleting main folder due to unsupported type: {}", path);
            deleteDirectory(path);
        } else if (Constants.GAME.equals(path.getFileName().toString())) {
            Set<Long> idsToCheck = getIdsToCheck(path);
            if (!idsToCheck.isEmpty()) {
                Set<Long> existingIds = getExistingIds(idsToCheck, gameRepository::findIdsByIds);
                removeInvalidSubdirectories(path, existingIds, "game");
            }
        } else if (Constants.PLATFORM.equals(path.getFileName().toString())) {
            Set<Long> idsToCheck = getIdsToCheck(path);
            if (!idsToCheck.isEmpty()) {
                Set<Long> existingIds = getExistingIds(idsToCheck, platformRepository::findIdsByIds);
                removeInvalidSubdirectories(path, existingIds, "platform");
            }
        }
    }

    @SneakyThrows
    private static Set<Long> getIdsToCheck(Path path) {
        Set<Long> idsToCheck;
        try (Stream<Path> files = Files.list(path)) {
            idsToCheck = files.map(MetadataService::pathToLong)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return idsToCheck;
    }

    private static Set<Long> getExistingIds(Set<Long> idsToCheck, Function<Iterable<Long>, Flux<Long>> checkingMethod) {
        return Mono.fromSupplier(() -> ListUtils.partition(new ArrayList<>(idsToCheck), PARTITION_SIZE))
                .flatMapMany(Flux::fromIterable)
                .flatMap(checkingMethod)
                .collectList()
                .map(Set::copyOf)
                .block();
    }

    @SneakyThrows
    private static void removeInvalidSubdirectories(Path path, Set<Long> existingIds, String objectName) {
        try (Stream<Path> files = Files.list(path)) {
            files.forEach(gamePath -> {
                Long id = pathToLong(gamePath);
                if (id != null) {
                    if (existingIds != null && !existingIds.contains(id)) {
                        log.debug(
                                "checkFolderAndRemoveInvalidFolders > deleting {} folder due to missing {} with id: {}",
                                objectName, objectName, gamePath);
                        deleteDirectory(gamePath);
                    }
                } else {
                    log.debug(
                            "checkFolderAndRemoveInvalidFolders > deleting {} folder due to invalid {} id: {}",
                            objectName, objectName, gamePath);
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
        return Mono.fromCallable(() -> {
            if (StringUtils.isBlank(metadataFolder)) {
                log.error("saveMetadata > ERROR, metadata folder is not specified!");
                throw new IllegalStateException("No metadata folder set!");
            }
            if (!ALLOWED_FILE_NAMES.contains(fieldName)) {
                log.warn("saveMetadata > ERROR, invalid field name: {}!", fieldName);
                throw new IllegalArgumentException("Invalid file name!");
            }

            doSaveMetadata(folder, idPart, filename, bytes, fieldName);
            return true;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private void doSaveMetadata(String folder, String idPart, String filename, byte[] bytes, String fieldName) throws
            IOException {
        Path dir = Path.of(metadataFolder, folder, idPart);
        String dirStr = dir.toString();

        Files.createDirectories(dir);
        deleteExistingMetadata(fieldName, dir);
        Path path = Path.of(dirStr, filename);
        deleteIfExists(path);
        log.debug("saveMetadata > Writing file: {}", path);
        Files.write(path, bytes);
    }

    private static void deleteExistingMetadata(String fieldName, Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            files.forEach(path -> {
                String existingFileExtension = FilenameUtils.getExtension(path.toString());
                String baseName = FilenameUtils.getBaseName(path.toString());
                if (existingFileExtension.equalsIgnoreCase("tmp")
                        || (baseName != null && baseName.equals(fieldName))) {
                    log.debug("deleteExistingMetadata > found same existing metadata, will try to remove: {}", path);
                    deleteIfExists(path);
                }
            });
        }
    }

    public Mono<Boolean> deleteMetadata(String folder, String idPart, String fieldName) {
        return Mono.fromCallable(() -> {
            if (StringUtils.isBlank(metadataFolder)) {
                log.error("deleteMetadata > ERROR, metadata folder is not specified!");
                throw new IllegalStateException("No metadata folder set!");
            }
            if (!ALLOWED_FILE_NAMES.contains(fieldName)) {
                log.warn("deleteMetadata > ERROR, invalid field name: {}!", fieldName);
                throw new IllegalArgumentException("Invalid file name!");
            }
            Path dir = Path.of(metadataFolder, folder, idPart);
            if (Files.exists(dir)) {
                return deleteExisting(fieldName, dir);
            }
            return false;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @SneakyThrows
    private static boolean deleteExisting(String fieldName, Path dir) {
        AtomicBoolean deletedAnything = new AtomicBoolean(false);
        try (Stream<Path> files = Files.list(dir)) {
            files.forEach(path -> {
                String existingFileExtension = FilenameUtils.getExtension(path.toString());
                String baseName = FilenameUtils.getBaseName(path.toString());
                if (existingFileExtension.equalsIgnoreCase(
                        "tmp") || (baseName != null && baseName.equals(fieldName))) {
                    boolean result = deleteIfExists(path);
                    if (result) {
                        deletedAnything.set(true);
                    }
                }
            });
        }
        return deletedAnything.get();
    }


    @SneakyThrows
    private static boolean deleteIfExists(Path path) {
        log.debug("Deleting file: {}", path);
        return Files.deleteIfExists(path);
    }

    public Mono<Tuple2<String, Flux<DataBuffer>>> getMetadata(String folder, String id, String filename) {
        return Mono.fromCallable(() -> findMetadataPath(folder, id, filename))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(Objects::nonNull)
                .flatMap(path ->
                        getFileName(path).zipWith(readFile(path))
                );
    }

    @SneakyThrows
    private Path findMetadataPath(String folder, String id, String filename) {
        Path directory = Path.of(metadataFolder, folder, id);
        try (Stream<Path> list = Files.list(directory)) {
            List<Path> matchingFiles = list.filter(path -> {
                String baseName = FilenameUtils.getBaseName(path.toString());
                return baseName != null && StringUtils.equals(baseName, filename);
            }).toList();
            if (matchingFiles.size() > 1) {
                log.warn("findMetadataPath > found multiple matching files for directory {} - {}!", directory,
                        matchingFiles);
            }
            return matchingFiles.stream().findFirst().orElse(null);
        }
    }

    private static Mono<String> getFileName(Path path) {
        return Mono.justOrEmpty(path).map(Path::getFileName).map(Path::toString);
    }

    private static Mono<Flux<DataBuffer>> readFile(Path path) {
        return Mono.fromCallable(
                        () -> DataBufferUtils.read(path, new DefaultDataBufferFactory(), 4096))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static Long pathToLong(Path gamePath) {
        return Try.of(() -> Long.parseLong(gamePath.getFileName().toString())).getOrNull();
    }
}
