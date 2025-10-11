package pl.yalgrin.playnite.simplesync.service.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import pl.yalgrin.playnite.simplesync.config.Constants;
import pl.yalgrin.playnite.simplesync.domain.objects.Game;
import pl.yalgrin.playnite.simplesync.domain.objects.GameDiff;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.*;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.exception.ForceFetchRequiredException;
import pl.yalgrin.playnite.simplesync.exception.ManualSynchronizationRequiredException;
import pl.yalgrin.playnite.simplesync.mapper.objects.GameMapper;
import pl.yalgrin.playnite.simplesync.repository.objects.GameRepository;
import pl.yalgrin.playnite.simplesync.service.ChangeListenerService;
import pl.yalgrin.playnite.simplesync.service.ChangeService;
import pl.yalgrin.playnite.simplesync.service.MetadataService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static pl.yalgrin.playnite.simplesync.utils.ReactorUtils.toMono;

@Service
@Slf4j
public class GameService extends AbstractObjectWithMetadataService<Game, GameDiff, GameDTO, GameDiffDTO> {

    private final GameRepository gameRepository;
    private final GenreService genreService;
    private final PlatformService platformService;
    private final CompanyService companyService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final FeatureService featureService;
    private final SeriesService seriesService;
    private final AgeRatingService ageRatingService;
    private final RegionService regionService;
    private final SourceService sourceService;
    private final CompletionStatusService completionStatusService;

    public GameService(GameRepository repository, R2dbcRepository<GameDiff, Long> diffRepository,
                       GameMapper mapper,
                       ChangeService changeService, ChangeListenerService changeListenerService,
                       MetadataService metadataService, ObjectMapper objectMapper,
                       TransactionalOperator transactionalOperator,
                       GenreService genreService, PlatformService platformService, CompanyService companyService,
                       CategoryService categoryService, TagService tagService, FeatureService featureService,
                       SeriesService seriesService, AgeRatingService ageRatingService, RegionService regionService,
                       SourceService sourceService, CompletionStatusService completionStatusService) {
        super(repository, diffRepository, mapper, changeService, changeListenerService, metadataService, objectMapper,
                transactionalOperator);
        this.gameRepository = repository;
        this.genreService = genreService;
        this.platformService = platformService;
        this.companyService = companyService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.featureService = featureService;
        this.seriesService = seriesService;
        this.ageRatingService = ageRatingService;
        this.regionService = regionService;
        this.sourceService = sourceService;
        this.completionStatusService = completionStatusService;
    }

    @Override
    public Mono<GameDTO> saveObject(GameDTO dto, String clientId, Flux<FilePart> fileParts, boolean saveFiles) {
        List<ChangeDTO> changes = Collections.synchronizedList(new ArrayList<>());
        return saveGameWithDependencies(dto, clientId, fileParts, saveFiles, changes)
                .as(transactionalOperator::transactional)
                .flatMap(
                        g -> Mono.defer(() -> changeListenerService.publishChanges(changes)).then(Mono.justOrEmpty(g)));
    }

    private Mono<GameDTO> saveGameWithDependencies(GameDTO dto, String clientId,
                                                   Flux<FilePart> fileParts,
                                                   boolean saveFiles, List<ChangeDTO> changes) {
        return saveGenres(dto, clientId, changes)
                .then(savePlatforms(dto, clientId, changes))
                .then(savePublishers(dto, clientId, changes))
                .then(saveDevelopers(dto, clientId, changes))
                .then(saveCategories(dto, clientId, changes))
                .then(saveTags(dto, clientId, changes))
                .then(saveFeatures(dto, clientId, changes))
                .then(saveSeries(dto, clientId, changes))
                .then(saveAgeRatings(dto, clientId, changes))
                .then(saveRegions(dto, clientId, changes))
                .then(saveSource(dto, clientId, changes))
                .then(saveCompletionStatus(dto, clientId, changes))
                .then(saveObjectWithoutPublishing(dto, clientId, fileParts, saveFiles).doOnNext(
                        t -> changes.add(t._2)).map(Tuple2::_1));
    }

    private Mono<List<Tuple2<GenreDTO, ChangeDTO>>> saveGenres(GameDTO dto, String clientId, List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getGenres, GameDTO::setGenres, genreService);
    }

    private Mono<List<Tuple2<PlatformDTO, ChangeDTO>>> savePlatforms(GameDTO dto, String clientId,
                                                                     List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getPlatforms, GameDTO::setPlatforms, platformService);
    }

    private Mono<List<Tuple2<CompanyDTO, ChangeDTO>>> savePublishers(GameDTO dto, String clientId,
                                                                     List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getPublishers, GameDTO::setPublishers, companyService);
    }

    private Mono<List<Tuple2<CompanyDTO, ChangeDTO>>> saveDevelopers(GameDTO dto, String clientId,
                                                                     List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getDevelopers, GameDTO::setDevelopers, companyService);
    }

    private Mono<List<Tuple2<CategoryDTO, ChangeDTO>>> saveCategories(GameDTO dto, String clientId,
                                                                      List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getCategories, GameDTO::setCategories, categoryService);
    }

    private Mono<List<Tuple2<TagDTO, ChangeDTO>>> saveTags(GameDTO dto, String clientId, List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getTags, GameDTO::setTags, tagService);
    }

    private Mono<List<Tuple2<FeatureDTO, ChangeDTO>>> saveFeatures(GameDTO dto, String clientId,
                                                                   List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getFeatures, GameDTO::setFeatures, featureService);
    }

    private Mono<List<Tuple2<SeriesDTO, ChangeDTO>>> saveSeries(GameDTO dto, String clientId, List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getSeries, GameDTO::setSeries, seriesService);
    }

    private Mono<List<Tuple2<AgeRatingDTO, ChangeDTO>>> saveAgeRatings(GameDTO dto, String clientId,
                                                                       List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getAgeRatings, GameDTO::setAgeRatings, ageRatingService);
    }

    private Mono<List<Tuple2<RegionDTO, ChangeDTO>>> saveRegions(GameDTO dto, String clientId,
                                                                 List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDTO::getRegions, GameDTO::setRegions, regionService);
    }

    private Mono<Tuple2<SourceDTO, ChangeDTO>> saveSource(GameDTO dto, String clientId, List<ChangeDTO> changes) {
        return saveObject(dto, clientId, changes, GameDTO::getSource, GameDTO::setSource, sourceService);
    }

    private Mono<Tuple2<CompletionStatusDTO, ChangeDTO>> saveCompletionStatus(GameDTO dto, String clientId,
                                                                              List<ChangeDTO> changes) {
        return saveObject(dto, clientId, changes, GameDTO::getCompletionStatus, GameDTO::setCompletionStatus,
                completionStatusService);
    }

    private <T extends AbstractObjectDTO> Mono<List<Tuple2<T, ChangeDTO>>> saveObjects(GameDTO dto, String clientId,
                                                                                       List<ChangeDTO> changes,
                                                                                       Function<GameDTO, List<T>> dtoGetter,
                                                                                       BiConsumer<GameDTO, List<T>> dtoSetter,
                                                                                       ObjectSaveService<T> service) {
        return Mono.fromSupplier(() -> dtoGetter.apply(dto))
                .flatMapMany(Flux::fromIterable)
                .concatMap(d -> service.saveObjectWithoutPublishing(d, clientId).switchIfEmpty(Mono.justOrEmpty(
                        Tuple.of(d, null)))).collectList()
                .doOnNext(t -> dtoSetter.accept(dto, t.stream().map(Tuple2::_1).toList()))
                .doOnNext(t -> t.stream().map(Tuple2::_2).filter(Objects::nonNull).forEach(changes::add));
    }

    private <T extends AbstractObjectDTO> Mono<Tuple2<T, ChangeDTO>> saveObject(GameDTO dto, String clientId,
                                                                                List<ChangeDTO> changes,
                                                                                Function<GameDTO, T> dtoGetter,
                                                                                BiConsumer<GameDTO, T> dtoSetter,
                                                                                ObjectSaveService<T> service) {
        return Mono.justOrEmpty(dtoGetter.apply(dto))
                .flatMap(d -> service.saveObjectWithoutPublishing(d, clientId)
                        .switchIfEmpty(Mono.justOrEmpty(Tuple.of(d, null))))
                .doOnNext(t -> dtoSetter.accept(dto, t._1))
                .doOnNext(t -> Optional.ofNullable(t._2).ifPresent(changes::add));
    }

    @Override
    public Mono<GameDTO> saveObjectDiff(GameDiffDTO gameDiffDTO, String clientId, Flux<FilePart> fileParts) {
        List<ChangeDTO> changes = Collections.synchronizedList(new ArrayList<>());
        return saveGameWithDependencies(gameDiffDTO, clientId, fileParts, changes).as(
                        transactionalOperator::transactional)
                .flatMap(
                        g -> Mono.defer(() -> changeListenerService.publishChanges(changes)).then(Mono.justOrEmpty(g)));
    }

    private Mono<GameDTO> saveGameWithDependencies(GameDiffDTO dto, String clientId,
                                                   Flux<FilePart> fileParts,
                                                   List<ChangeDTO> changes) {
        return saveGenres(dto, clientId, changes)
                .then(savePlatforms(dto, clientId, changes))
                .then(savePublishers(dto, clientId, changes))
                .then(saveDevelopers(dto, clientId, changes))
                .then(saveCategories(dto, clientId, changes))
                .then(saveTags(dto, clientId, changes))
                .then(saveFeatures(dto, clientId, changes))
                .then(saveSeries(dto, clientId, changes))
                .then(saveAgeRatings(dto, clientId, changes))
                .then(saveRegions(dto, clientId, changes))
                .then(saveSource(dto, clientId, changes))
                .then(saveCompletionStatus(dto, clientId, changes))
                .then(saveObjectDiffWithoutPublishing(dto, clientId, fileParts).doOnNext(
                        t -> changes.add(t._2)).map(Tuple2::_1));
    }

    private <T extends AbstractObjectDTO> Mono<List<Tuple2<T, ChangeDTO>>> saveObjects(GameDiffDTO dto, String clientId,
                                                                                       List<ChangeDTO> changes,
                                                                                       Function<GameDiffDTO, List<T>> dtoGetter,
                                                                                       BiConsumer<GameDiffDTO, List<T>> dtoSetter,
                                                                                       ObjectSaveService<T> service,
                                                                                       String fieldName) {
        if (dto.getChangedFields() == null || !dto.getChangedFields().contains(fieldName)) {
            return Mono.empty();
        }
        return Mono.fromSupplier(() -> dtoGetter.apply(dto))
                .flatMapMany(Flux::fromIterable)
                .concatMap(d -> service.saveObjectWithoutPublishing(d, clientId).switchIfEmpty(Mono.justOrEmpty(
                        Tuple.of(d, null)))).collectList()
                .doOnNext(t -> dtoSetter.accept(dto, t.stream().map(Tuple2::_1).toList()))
                .doOnNext(t -> t.stream().map(Tuple2::_2).filter(Objects::nonNull).forEach(changes::add));
    }

    private <T extends AbstractObjectDTO> Mono<Tuple2<T, ChangeDTO>> saveObject(GameDiffDTO dto, String clientId,
                                                                                List<ChangeDTO> changes,
                                                                                Function<GameDiffDTO, T> dtoGetter,
                                                                                BiConsumer<GameDiffDTO, T> dtoSetter,
                                                                                ObjectSaveService<T> service,
                                                                                String fieldName) {
        if (dto.getChangedFields() == null || !dto.getChangedFields().contains(fieldName)) {
            return Mono.empty();
        }
        return Mono.justOrEmpty(dtoGetter.apply(dto))
                .flatMap(d -> service.saveObjectWithoutPublishing(d, clientId)
                        .switchIfEmpty(Mono.justOrEmpty(Tuple.of(d, null))))
                .doOnNext(t -> dtoSetter.accept(dto, t._1))
                .doOnNext(t -> Optional.ofNullable(t._2).ifPresent(changes::add));
    }

    private Mono<List<Tuple2<GenreDTO, ChangeDTO>>> saveGenres(GameDiffDTO dto, String clientId,
                                                               List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getGenres, GameDiffDTO::setGenres, genreService,
                GameDiffDTO.Fields.GENRES);
    }

    private Mono<List<Tuple2<PlatformDTO, ChangeDTO>>> savePlatforms(GameDiffDTO dto, String clientId,
                                                                     List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getPlatforms, GameDiffDTO::setPlatforms,
                platformService, GameDiffDTO.Fields.PLATFORMS);
    }

    private Mono<List<Tuple2<CompanyDTO, ChangeDTO>>> savePublishers(GameDiffDTO dto, String clientId,
                                                                     List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getPublishers, GameDiffDTO::setPublishers,
                companyService, GameDiffDTO.Fields.PUBLISHERS);
    }

    private Mono<List<Tuple2<CompanyDTO, ChangeDTO>>> saveDevelopers(GameDiffDTO dto, String clientId,
                                                                     List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getDevelopers, GameDiffDTO::setDevelopers,
                companyService, GameDiffDTO.Fields.DEVELOPERS);
    }

    private Mono<List<Tuple2<CategoryDTO, ChangeDTO>>> saveCategories(GameDiffDTO dto, String clientId,
                                                                      List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getCategories, GameDiffDTO::setCategories,
                categoryService, GameDiffDTO.Fields.CATEGORIES);
    }

    private Mono<List<Tuple2<TagDTO, ChangeDTO>>> saveTags(GameDiffDTO dto, String clientId, List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getTags, GameDiffDTO::setTags, tagService,
                GameDiffDTO.Fields.TAGS);
    }

    private Mono<List<Tuple2<FeatureDTO, ChangeDTO>>> saveFeatures(GameDiffDTO dto, String clientId,
                                                                   List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getFeatures, GameDiffDTO::setFeatures, featureService,
                GameDiffDTO.Fields.FEATURES);
    }

    private Mono<List<Tuple2<SeriesDTO, ChangeDTO>>> saveSeries(GameDiffDTO dto, String clientId,
                                                                List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getSeries, GameDiffDTO::setSeries, seriesService,
                GameDiffDTO.Fields.SERIES);
    }

    private Mono<List<Tuple2<AgeRatingDTO, ChangeDTO>>> saveAgeRatings(GameDiffDTO dto, String clientId,
                                                                       List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getAgeRatings, GameDiffDTO::setAgeRatings,
                ageRatingService, GameDiffDTO.Fields.AGE_RATINGS);
    }

    private Mono<List<Tuple2<RegionDTO, ChangeDTO>>> saveRegions(GameDiffDTO dto, String clientId,
                                                                 List<ChangeDTO> changes) {
        return saveObjects(dto, clientId, changes, GameDiffDTO::getRegions, GameDiffDTO::setRegions, regionService,
                GameDiffDTO.Fields.REGIONS);
    }

    private Mono<Tuple2<SourceDTO, ChangeDTO>> saveSource(GameDiffDTO dto, String clientId, List<ChangeDTO> changes) {
        return saveObject(dto, clientId, changes, GameDiffDTO::getSource, GameDiffDTO::setSource, sourceService,
                GameDiffDTO.Fields.SOURCE);
    }

    private Mono<Tuple2<CompletionStatusDTO, ChangeDTO>> saveCompletionStatus(GameDiffDTO dto, String clientId,
                                                                              List<ChangeDTO> changes) {
        return saveObject(dto, clientId, changes, GameDiffDTO::getCompletionStatus, GameDiffDTO::setCompletionStatus,
                completionStatusService, GameDiffDTO.Fields.COMPLETION_STATUS);
    }

    @Override
    protected Mono<Game> findOrCreateEntity(GameDTO dto) {
        return toMono(gameRepository.findByGameIdAndPluginId(dto.getGameId(), dto.getPluginId()))
                .doOnNext(e -> {
                    log.debug("findOrCreateEntity > found entity with id = {} by game id: {} and plugin id: {}",
                            e.getId(),
                            dto.getGameId(), dto.getPluginId());
                    if (!e.isRemoved() && !StringUtils.equals(e.getPlayniteId(), dto.getId())) {
                        throw new ForceFetchRequiredException();
                    }
                })
                .switchIfEmpty(Mono.fromSupplier(() -> createEntityFromDTO(dto))
                        .doOnNext(e -> log.debug("findOrCreateEntity > creating new entity...")));
    }

    @Override
    protected Mono<Game> findOrCreateEntity(GameDiffDTO dto) {
        return toMono(gameRepository.findByGameIdAndPluginId(dto.getGameId(), dto.getPluginId()))
                .doOnNext(e -> {
                    log.debug("findOrCreateEntity > found entity with id = {} by game id: {} and plugin id: {}",
                            e.getId(),
                            dto.getGameId(), dto.getPluginId());
                    if (e.isRemoved()) {
                        throw new ManualSynchronizationRequiredException("Manual synchronization required!");
                    }
                    if (!StringUtils.equals(e.getPlayniteId(), dto.getId())) {
                        throw new ForceFetchRequiredException();
                    }
                })
                .switchIfEmpty(
                        Mono.error(new ManualSynchronizationRequiredException("Manual synchronization required!")));
    }

    @Override
    protected Set<String> getMetadataFields() {
        return Set.of(Constants.ICON, Constants.COVER_IMAGE, Constants.BACKGROUND_IMAGE);
    }

    @Override
    protected void setHex(Game game, String baseName, String md5) {
        if (Constants.ICON.equals(baseName)) {
            game.setIconMd5(md5);
            game.setChanged(true);
        } else if (Constants.COVER_IMAGE.equals(baseName)) {
            game.setCoverImageMd5(md5);
            game.setChanged(true);
        } else if (Constants.BACKGROUND_IMAGE.equals(baseName)) {
            game.setBackgroundImageMd5(md5);
            game.setChanged(true);
        }
    }

    @Override
    protected boolean shouldSaveMetadata(Game game, byte[] bytes, String md5, String basename) {
        if (game.getId() == null) {
            return true;
        }
        String md5ToCompare = null;
        if (Constants.ICON.equals(basename)) {
            md5ToCompare = game.getIconMd5();
        } else if (Constants.COVER_IMAGE.equals(basename)) {
            md5ToCompare = game.getCoverImageMd5();
        } else if (Constants.BACKGROUND_IMAGE.equals(basename)) {
            md5ToCompare = game.getBackgroundImageMd5();
        }
        return md5ToCompare == null || !StringUtils.equals(md5ToCompare, md5);
    }

    @Override
    protected Flux<Game> findObjectToDelete(GameDTO categoryDTO) {
        return gameRepository.findByPlayniteIdAndGameIdAndPluginIdAndRemovedIsFalse(categoryDTO.getId(),
                categoryDTO.getGameId(), categoryDTO.getPluginId());
    }

    @Override
    protected String getMetadataFolder() {
        return Constants.GAME;
    }

    @Override
    protected Game createEntityFromDTO(GameDTO dto) {
        return Game.builder().playniteId(dto.getId()).build();
    }

    @Override
    protected ObjectType getObjectType() {
        return ObjectType.Game;
    }

    @Override
    protected ObjectType getDiffType() {
        return ObjectType.GameDiff;
    }

    @Override
    protected Class<GameDiffDTO> getDiffDtoClass() {
        return GameDiffDTO.class;
    }
}
