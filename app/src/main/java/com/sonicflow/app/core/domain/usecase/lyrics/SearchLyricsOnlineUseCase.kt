package com.sonicflow.app.core.domain.usecase.lyrics

import com.sonicflow.app.core.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchLyricsOnlineUseCase @Inject constructor() {

    operator fun invoke(title: String, artist: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        try {
            // TODO: Appeler une API de lyrics
            emit(Resource.Error("Online lyrics search not implemented yet"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to search lyrics"))
        }
    }
}