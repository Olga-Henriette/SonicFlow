package com.sonicflow.app.core.domain.usecase.lyrics

import com.sonicflow.app.core.data.local.dao.LyricsDao
import javax.inject.Inject

class DeleteLyricsUseCase @Inject constructor(
    private val lyricsDao: LyricsDao
) {
    suspend operator fun invoke(songId: Long) {
        lyricsDao.deleteLyrics(songId)
    }
}