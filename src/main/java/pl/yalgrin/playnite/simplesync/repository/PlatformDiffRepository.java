package pl.yalgrin.playnite.simplesync.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.PlatformDiff;

@Repository
public interface PlatformDiffRepository extends R2dbcRepository<PlatformDiff, Long> {
}
