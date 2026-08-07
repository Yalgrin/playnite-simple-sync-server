package pl.yalgrin.playnite.simplesync.library.repository

import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import pl.yalgrin.playnite.simplesync.library.domain.PlatformDiff

@Repository
interface PlatformDiffRepository : R2dbcRepository<PlatformDiff, Long>