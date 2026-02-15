package com.sonicflow.app.core.domain.usecase.lyrics

import com.sonicflow.app.core.data.local.entity.LyricsEntity
import com.sonicflow.app.core.data.local.dao.LyricsDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLyricsUseCase @Inject constructor(
    private val lyricsDao: LyricsDao
) {
    operator fun invoke(songId: Long): Flow<LyricsEntity?> {
        return lyricsDao.getLyrics(songId)
    }
}