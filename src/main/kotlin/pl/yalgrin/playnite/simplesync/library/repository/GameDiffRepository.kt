package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.GameDiff

@Repository
interface GameDiffRepository : R2dbcRepository<GameDiff, Long>