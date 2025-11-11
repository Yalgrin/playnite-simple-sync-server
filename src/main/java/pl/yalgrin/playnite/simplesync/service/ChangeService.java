package pl.yalgrin.playnite.simplesync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.yalgrin.playnite.simplesync.domain.objects.Game;
import pl.yalgrin.playnite.simplesync.dto.ChangeDTO;
import pl.yalgrin.playnite.simplesync.dto.GameChangeRequestDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.AbstractObjectDTO;
import pl.yalgrin.playnite.simplesync.dto.objects.GameDTO;
import pl.yalgrin.playnite.simplesync.enums.ObjectType;
import pl.yalgrin.playnite.simplesync.mapper.ChangeMapper;
import pl.yalgrin.playnite.simplesync.repository.ChangeRepository;
import pl.yalgrin.playnite.simplesync.repository.objects.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class ChangeService {
    private final ChangeRepository repository;
    private final ChangeMapper mapper;
    private final ObjectMapper objectMapper;

    private final GameRepository gameRepository;
    private final Map<ObjectType, ObjectRepository<?>> relatedObjectRepositories;

    public ChangeService(ChangeRepository repository, ChangeMapper mapper, ObjectMapper objectMapper,
                         CategoryRepository categoryRepository, GenreRepository genreRepository,
                         PlatformRepository platformRepository, CompanyRepository companyRepository,
                         FeatureRepository featureRepository, TagRepository tagRepository,
                         SeriesRepository seriesRepository, AgeRatingRepository ageRatingRepository,
                         RegionRepository regionRepository, SourceRepository sourceRepository,
                         CompletionStatusRepository completionStatusRepository,
                         FilterPresetRepository filterPresetRepository, GameRepository gameRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.gameRepository = gameRepository;
        Map<ObjectType, ObjectRepository<?>> repositoryMap = new LinkedHashMap<>();
        repositoryMap.put(ObjectType.Category, categoryRepository);
        repositoryMap.put(ObjectType.Genre, genreRepository);
        repositoryMap.put(ObjectType.Platform, platformRepository);
        repositoryMap.put(ObjectType.Company, companyRepository);
        repositoryMap.put(ObjectType.Feature, featureRepository);
        repositoryMap.put(ObjectType.Tag, tagRepository);
        repositoryMap.put(ObjectType.Series, seriesRepository);
        repositoryMap.put(ObjectType.AgeRating, ageRatingRepository);
        repositoryMap.put(ObjectType.Region, regionRepository);
        repositoryMap.put(ObjectType.Source, sourceRepository);
        repositoryMap.put(ObjectType.CompletionStatus, completionStatusRepository);
        repositoryMap.put(ObjectType.FilterPreset, filterPresetRepository);
        this.relatedObjectRepositories = repositoryMap;
    }

    @Transactional(readOnly = true)
    public Flux<ChangeDTO> findFromLastId(Long lastId) {
        return repository.findFromLastId(lastId).map(mapper::toDTO);
    }

    public Flux<ChangeDTO> generateChangesForAllObjects() {
        return findMaxId()
                .flatMapMany(maxId ->
                        Flux.mergeSequential(
                                Flux.fromIterable(relatedObjectRepositories.entrySet())
                                        .flatMapSequential(
                                                entry -> findChangesForObjectType(entry.getValue().findAllIds(), maxId,
                                                        entry.getKey())),
                                findChangesForObjectType(gameRepository.findAllIds(), maxId, ObjectType.Game))
                );
    }

    private Mono<Long> findMaxId() {
        return repository.findMaxId().switchIfEmpty(Mono.justOrEmpty(0L));
    }

    private Flux<ChangeDTO> findChangesForObjectType(Flux<Long> ids, Long maxId, ObjectType type) {
        return ids
                .map(id -> ChangeDTO.builder()
                        .id(maxId)
                        .type(type)
                        .objectId(id)
                        .build()
                );
    }

    public Mono<ChangeDTO> saveChange(ChangeDTO changeDTO) {
        return Mono.justOrEmpty(changeDTO)
                .map(mapper::toEntity)
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    public Flux<ChangeDTO> generateChangesForGames(GameChangeRequestDTO dto) {
        return Mono.justOrEmpty(dto)
                .filter(d -> CollectionUtils.isNotEmpty(d.getIds()) || CollectionUtils.isNotEmpty(d.getGameIds()))
                .flatMapMany(d -> {
                    CollectedIds collectedIds = new CollectedIds();
                    return fetchGames(d)
                            .doOnNext(game -> {
                                extractObjectUuids(game, collectedIds);
                                collectedIds.getGameIds().add(game.getId());
                            }).then(findIdsForUuids(collectedIds))
                            .thenMany(Flux.mergeSequential(
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getCategoryIds())
                                            .sort(), null, ObjectType.Category),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getGenreIds())
                                            .sort(), null, ObjectType.Genre),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getPlatformIds())
                                            .sort(), null, ObjectType.Platform),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getCompanyIds())
                                            .sort(), null, ObjectType.Company),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getFeatureIds())
                                            .sort(), null, ObjectType.Feature),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getTagIds())
                                            .sort(), null, ObjectType.Tag),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getSeriesIds())
                                            .sort(), null, ObjectType.Series),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getAgeRatingIds())
                                            .sort(), null, ObjectType.AgeRating),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getRegionIds())
                                            .sort(), null, ObjectType.Region),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getSourceIds())
                                            .sort(), null, ObjectType.Source),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getCompletionStatusIds())
                                            .sort(), null, ObjectType.CompletionStatus),
                                    findChangesForObjectType(Flux.fromIterable(collectedIds.getGameIds())
                                            .sort(), null, ObjectType.Game)
                            ));
                });
    }

    private void extractObjectUuids(Game game, CollectedIds collectedIds) {
        GameDTO targetDto = Try.of(
                        () -> objectMapper.readValue(game.getSavedData().asArray(), GameDTO.class))
                .getOrElse(GameDTO::new);
        if (targetDto.getCategories() != null) {
            targetDto.getCategories().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getCategoryUuids()::add);
        }
        if (targetDto.getGenres() != null) {
            targetDto.getGenres().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getGenreUuids()::add);
        }
        if (targetDto.getPlatforms() != null) {
            targetDto.getPlatforms().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getPlatformUuids()::add);
        }
        if (targetDto.getDevelopers() != null) {
            targetDto.getDevelopers().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getCompanyUuids()::add);
        }
        if (targetDto.getPublishers() != null) {
            targetDto.getPublishers().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getCompanyUuids()::add);
        }
        if (targetDto.getFeatures() != null) {
            targetDto.getFeatures().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getFeatureUuids()::add);
        }
        if (targetDto.getTags() != null) {
            targetDto.getTags().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getTagUuids()::add);
        }
        if (targetDto.getSeries() != null) {
            targetDto.getSeries().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getSeriesUuids()::add);
        }
        if (targetDto.getAgeRatings() != null) {
            targetDto.getAgeRatings().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getAgeRatingUuids()::add);
        }
        if (targetDto.getRegions() != null) {
            targetDto.getRegions().stream().map(AbstractObjectDTO::getId)
                    .forEach(collectedIds.getRegionUuids()::add);
        }
        Optional.ofNullable(targetDto.getSource())
                .map(AbstractObjectDTO::getId)
                .ifPresent(id -> collectedIds.getSourceUuids().add(id));
        Optional.ofNullable(targetDto.getCompletionStatus())
                .map(AbstractObjectDTO::getId)
                .ifPresent(id -> collectedIds.getCompletionStatusUuids().add(id));
    }

    private Mono<Void> findIdsForUuids(CollectedIds collectedIds) {
        return Mono.when(findCategories(collectedIds),
                findGenres(collectedIds),
                findPlatforms(collectedIds),
                findCompanies(collectedIds),
                findFeatures(collectedIds),
                findTags(collectedIds),
                findSeries(collectedIds),
                findAgeRatings(collectedIds),
                findRegions(collectedIds),
                findSources(collectedIds),
                findCompletionStatuses(collectedIds));
    }

    private Flux<Game> fetchGames(GameChangeRequestDTO dto) {
        Set<String> alreadyFetchedIds = Collections.synchronizedSet(new HashSet<>());
        return getByIdPairs(dto, alreadyFetchedIds)
                .flatMapMany(list -> Flux.merge(Flux.fromIterable(list), getByIds(dto, alreadyFetchedIds)));
    }

    private Mono<List<Game>> getByIdPairs(GameChangeRequestDTO dto, Set<String> alreadyFetchedIds) {
        if (dto == null || dto.getGameIds() == null) {
            return Mono.just(new ArrayList<>());
        }
        return Flux.fromIterable(dto.getGameIds())
                .filter(i -> i != null && i.getGameId() != null && i.getPluginId() != null)
                .flatMap(i -> gameRepository.findByGameIdAndPluginId(i.getGameId(), i.getPluginId()))
                .doOnNext(g -> alreadyFetchedIds.add(g.getPlayniteId()))
                .collectList().switchIfEmpty(Mono.just(new ArrayList<>()));
    }

    private Flux<Game> getByIds(GameChangeRequestDTO dto, Set<String> alreadyFetchedIds) {
        if (dto == null || dto.getIds() == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(dto.getIds()).filter(i -> !alreadyFetchedIds.contains(i)).buffer(100)
                .flatMap(gameRepository::findByPlayniteIdIn);
    }

    private Mono<Void> findCategories(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getCategoryUuids, CollectedIds::getCategoryIds,
                ObjectType.Category);
    }

    private Mono<Void> findGenres(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getGenreUuids, CollectedIds::getGenreIds,
                ObjectType.Genre);
    }

    private Mono<Void> findPlatforms(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getPlatformUuids, CollectedIds::getPlatformIds,
                ObjectType.Platform);
    }

    private Mono<Void> findCompanies(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getCompanyUuids, CollectedIds::getCompanyIds,
                ObjectType.Company);
    }

    private Mono<Void> findFeatures(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getFeatureUuids, CollectedIds::getFeatureIds,
                ObjectType.Feature);
    }

    private Mono<Void> findTags(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getTagUuids, CollectedIds::getTagIds,
                ObjectType.Tag);
    }

    private Mono<Void> findSeries(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getSeriesUuids, CollectedIds::getSeriesIds,
                ObjectType.Series);
    }

    private Mono<Void> findAgeRatings(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getAgeRatingUuids, CollectedIds::getAgeRatingIds,
                ObjectType.AgeRating);
    }

    private Mono<Void> findRegions(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getRegionUuids, CollectedIds::getRegionIds,
                ObjectType.Region);
    }

    private Mono<Void> findSources(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getSourceUuids, CollectedIds::getSourceIds,
                ObjectType.Source);
    }

    private Mono<Void> findCompletionStatuses(CollectedIds collectedIds) {
        return findIdsForUuidsForObject(collectedIds, CollectedIds::getCompletionStatusUuids,
                CollectedIds::getCompletionStatusIds, ObjectType.CompletionStatus);
    }

    private Mono<Void> findIdsForUuidsForObject(CollectedIds collectedIds,
                                                Function<CollectedIds, Set<String>> uuidExtractor,
                                                Function<CollectedIds, Set<Long>> idExtractor, ObjectType objectType) {
        return Mono.just(collectedIds)
                .map(uuidExtractor)
                .flatMapMany(Flux::fromIterable)
                .buffer(100)
                .flatMap(ids -> relatedObjectRepositories.get(objectType).findIdsByPlayniteIdIn(ids))
                .doOnNext(id -> idExtractor.apply(collectedIds).add(id)).then();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CollectedIds {
        private Set<String> categoryUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> categoryIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> genreUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> genreIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> platformUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> platformIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> companyUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> companyIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> featureUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> featureIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> tagUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> tagIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> seriesUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> seriesIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> ageRatingUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> ageRatingIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> regionUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> regionIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> sourceUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> sourceIds = Collections.synchronizedSet(new HashSet<>());
        private Set<String> completionStatusUuids = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> completionStatusIds = Collections.synchronizedSet(new HashSet<>());
        private Set<Long> gameIds = Collections.synchronizedSet(new HashSet<>());
    }
}
