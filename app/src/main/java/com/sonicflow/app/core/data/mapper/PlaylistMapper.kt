package com.sonicflow.app.core.data.mapper

import com.sonicflow.app.core.data.local.entity.PlaylistEntity
import com.sonicflow.app.core.domain.model.Playlist

/**
 * Convertit PlaylistEntity (Room) en Playlist (Domain)
 */
fun PlaylistEntity.toDomain(songCount: Int): Playlist {
    return Playlist(
        id = id,
        name = name,
        songCount = songCount,
        dateCreated = dateCreated,
        dateModified = dateModified,
        artworkUri = artworkUri
    )
}

fun Playlist.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        id = id,
        name = name,
        dateCreated = dateCreated,
        dateModified = dateModified,
        artworkUri = artworkUri
    )
}