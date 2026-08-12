package pl.yalgrin.playnite.simplesync.library.service

import pl.yalgrin.playnite.simplesync.change.dto.ChangeDTO

data class LibrarySaveResult<T>(
    val savedObject: T,
    val generatedChanges: List<ChangeDTO>
)