package pl.yalgrin.playnite.simplesync.repository.objects;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pl.yalgrin.playnite.simplesync.domain.objects.GameDiff;

@Repository
public interface GameDiffRepository extends R2dbcRepository<GameDiff, Long> {
}
