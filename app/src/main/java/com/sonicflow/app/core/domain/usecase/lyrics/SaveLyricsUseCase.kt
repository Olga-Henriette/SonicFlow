package com.sonicflow.app.core.domain.usecase.lyrics

import com.sonicflow.app.core.data.local.entity.LyricsEntity
import com.sonicflow.app.core.data.local.dao.LyricsDao
import javax.inject.Inject

class SaveLyricsUseCase @Inject constructor(
    private val lyricsDao: LyricsDao
) {
    suspend operator fun invoke(lyrics: LyricsEntity) {
        lyricsDao.insertLyrics(lyrics)
    }
}