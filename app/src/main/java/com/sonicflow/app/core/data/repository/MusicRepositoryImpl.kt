// core/data/repository/MusicRepositoryImpl.kt
package com.sonicflow.app.core.data.repository

import com.sonicflow.app.core.common.Resource
import com.sonicflow.app.core.data.local.dao.FavoriteDao
import com.sonicflow.app.core.data.local.dao.PlayHistoryDao
import com.sonicflow.app.core.data.local.dao.PlaylistDao
import com.sonicflow.app.core.data.local.entity.PlaylistEntity
import com.sonicflow.app.core.data.local.entity.PlaylistSongCrossRef
import com.sonicflow.app.core.data.mapper.toDomain
import com.sonicflow.app.core.data.mapper.toEntity
import com.sonicflow.app.core.data.source.local.MediaStoreDataSource
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.domain.model.Artist
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation du MusicRepository
 * - Combine les données de MediaStore (fichiers) et Room (playlists/favoris)
 * - Gère les erreurs et transforme en Resource<T>
 */
@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val playHistoryDao: PlayHistoryDao
) : MusicRepository {

    // ==================== SONGS ====================

    override fun getAllSongs(): Flow<Resource<List<Song>>> = flow {
        emit(Resource.Loading())

        mediaStoreDataSource.getAllSongs()
            .combine(favoriteDao.getFavoriteSongIds()) { songs, favoriteIds ->
                // Marque les chansons favorites
                songs.map { song ->
                    song.copy(isFavorite = favoriteIds.contains(song.id))
                }
            }
            .catch { e ->
                Timber.e(e, "Error loading songs")
                emit(Resource.Error("Failed to load songs: ${e.message}"))
            }
            .collect { songs ->
                emit(Resource.Success(songs))
            }
    }

    override suspend fun getSongById(id: Long): Song? {
        // Pour l'instant, on scan toutes les chansons (optimisable plus tard)
        var foundSong: Song? = null
        getAllSongs().collect { resource ->
            if (resource is Resource.Success) {
                foundSong = resource.data?.find { it.id == id }
            }
        }
        return foundSong
    }

    override fun searchSongs(query: String): Flow<List<Song>> = flow {
        getAllSongs().collect { resource ->
            if (resource is Resource.Success) {
                val filtered = resource.data?.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.artist.contains(query, ignoreCase = true) ||
                            it.album.contains(query, ignoreCase = true)
                } ?: emptyList()
                emit(filtered)
            }
        }
    }

    // ==================== ALBUMS ====================

    override fun getAllAlbums(): Flow<Resource<List<Album>>> = flow {
        emit(Resource.Loading())

        mediaStoreDataSource.getAllAlbums()
            .catch { e ->
                Timber.e(e, "Error loading albums")
                emit(Resource.Error("Failed to load albums: ${e.message}"))
            }
            .collect { albums ->
                emit(Resource.Success(albums))
            }
    }

    override suspend fun getAlbumById(id: Long): Album? {
        var foundAlbum: Album? = null
        getAllAlbums().collect { resource ->
            if (resource is Resource.Success) {
                foundAlbum = resource.data?.find { it.id == id }
            }
        }
        return foundAlbum
    }

    override fun getAlbumSongs(albumId: Long): Flow<List<Song>> = flow {
        getAllSongs().collect { resource ->
            if (resource is Resource.Success) {
                val filtered = resource.data?.filter { it.albumId == albumId } ?: emptyList()
                emit(filtered)
            }
        }
    }

    // ==================== ARTISTS ====================

    override fun getAllArtists(): Flow<Resource<List<Artist>>> = flow {
        emit(Resource.Loading())

        mediaStoreDataSource.getAllArtists()
            .catch { e ->
                Timber.e(e, "Error loading artists")
                emit(Resource.Error("Failed to load artists: ${e.message}"))
            }
            .collect { artists ->
                emit(Resource.Success(artists))
            }
    }

    override suspend fun getArtistById(id: Long): Artist? {
        var foundArtist: Artist? = null
        getAllArtists().collect { resource ->
            if (resource is Resource.Success) {
                foundArtist = resource.data?.find { it.id == id }
            }
        }
        return foundArtist
    }

    override fun getArtistSongs(artistId: Long): Flow<List<Song>> = flow {
        getAllSongs().collect { resource ->
            if (resource is Resource.Success) {
                val filtered = resource.data?.filter {
                    it.artist.equals(getArtistById(artistId)?.name, ignoreCase = true)
                } ?: emptyList()
                emit(filtered)
            }
        }
    }

    override fun getArtistAlbums(artistId: Long): Flow<List<Album>> = flow {
        getAllAlbums().collect { resource ->
            if (resource is Resource.Success) {
                val filtered = resource.data?.filter { it.artistId == artistId } ?: emptyList()
                emit(filtered)
            }
        }
    }

    // ==================== PLAYLISTS ====================

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                val songCount = playlistDao.getPlaylistSongCount(entity.id)
                entity.toDomain(songCount)
            }
        }
    }

    override suspend fun createPlaylist(name: String): Long {
        val playlist = PlaylistEntity(
            name = name,
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
        return playlistDao.insertPlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val currentCount = playlistDao.getPlaylistSongCount(playlistId)
        val crossRef = PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = songId,
            position = currentCount
        )
        playlistDao.addSongToPlaylist(crossRef)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylistById(playlistId, songId)
    }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> = flow {
        playlistDao.getPlaylistSongIds(playlistId).collect { songIds ->
            getAllSongs().collect { resource ->
                if (resource is Resource.Success) {
                    val songs = resource.data?.filter { songIds.contains(it.id) } ?: emptyList()
                    // Trier par position dans la playlist
                    val sortedSongs = songIds.mapNotNull { id -> songs.find { it.id == id } }
                    emit(sortedSongs)
                }
            }
        }
    }

    // ==================== FAVORITES ====================

    override suspend fun toggleFavorite(songId: Long) {
        favoriteDao.toggleFavorite(songId)
    }

    override fun getFavoriteSongs(): Flow<List<Song>> = flow {
        favoriteDao.getFavoriteSongIds().collect { favoriteIds ->
            getAllSongs().collect { resource ->
                if (resource is Resource.Success) {
                    val favorites = resource.data?.filter { favoriteIds.contains(it.id) } ?: emptyList()
                    emit(favorites)
                }
            }
        }
    }

    // ==================== RECENT / STATS ====================

    override suspend fun incrementPlayCount(songId: Long) {
        playHistoryDao.incrementPlayCount(songId)
    }

    override fun getRecentlyPlayed(limit: Int): Flow<List<Song>> = flow {
        playHistoryDao.getRecentlyPlayedSongIds(limit).collect { recentIds ->
            getAllSongs().collect { resource ->
                if (resource is Resource.Success) {
                    val songs = resource.data?.filter { recentIds.contains(it.id) } ?: emptyList()
                    // Trier par ordre des IDs récents
                    val sortedSongs = recentIds.mapNotNull { id -> songs.find { it.id == id } }
                    emit(sortedSongs)
                }
            }
        }
    }

    override fun getMostPlayed(limit: Int): Flow<List<Song>> = flow {
        playHistoryDao.getMostPlayedSongIds(limit).collect { mostPlayedIds ->
            getAllSongs().collect { resource ->
                if (resource is Resource.Success) {
                    val songs = resource.data?.filter { mostPlayedIds.contains(it.id) } ?: emptyList()
                    val sortedSongs = mostPlayedIds.mapNotNull { id -> songs.find { it.id == id } }
                    emit(sortedSongs)
                }
            }
        }
    }
}