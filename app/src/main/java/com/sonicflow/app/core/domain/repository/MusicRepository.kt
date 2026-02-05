package com.sonicflow.app.core.domain.repository

import com.sonicflow.app.core.common.Resource
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.domain.model.Artist
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    // SONGS

    /**
     * Récupère toutes les chansons
     * @return Flow qui émet la liste à chaque changement
     */
    fun getAllSongs(): Flow<Resource<List<Song>>>

    /**
     * Récupère une chanson par ID
     */
    suspend fun getSongById(id: Long): Song?

    /**
     * Recherche de chansons par titre/artiste
     */
    fun searchSongs(query: String): Flow<List<Song>>

    // ALBUMS

    fun getAllAlbums(): Flow<Resource<List<Album>>>
    suspend fun getAlbumById(id: Long): Album?
    fun getAlbumSongs(albumId: Long): Flow<List<Song>>

    // ARTISTS

    fun getAllArtists(): Flow<Resource<List<Artist>>>
    suspend fun getArtistById(id: Long): Artist?
    fun getArtistSongs(artistId: Long): Flow<List<Song>>
    fun getArtistAlbums(artistId: Long): Flow<List<Album>>

    // PLAYLISTS

    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>

    // FAVORITES

    suspend fun toggleFavorite(songId: Long)
    fun getFavoriteSongs(): Flow<List<Song>>

    // RECENT / STATS

    suspend fun incrementPlayCount(songId: Long)
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<Song>>
    fun getMostPlayed(limit: Int = 20): Flow<List<Song>>
}