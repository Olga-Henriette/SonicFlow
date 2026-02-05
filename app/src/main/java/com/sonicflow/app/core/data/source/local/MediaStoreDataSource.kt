package com.sonicflow.app.core.data.source.local

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.domain.model.Artist
import com.sonicflow.app.core.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

/**
 * Source de données MediaStore
 * - Scanne les fichiers audio du téléphone
 * - Utilise ContentResolver pour accéder à MediaStore
 * - Convertit les Cursor en Domain Models
 */
class MediaStoreDataSource @Inject constructor(
    private val contentResolver: ContentResolver
) {

    companion object {
        // URI de base pour les fichiers audio
        private val AUDIO_URI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        // Colonnes à récupérer
        private val SONG_PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.BITRATE
        )

        // Filtre : uniquement de la musique (pas les alarmes, notifications, etc.)
        private const val SONG_SELECTION = "${MediaStore.Audio.Media.IS_MUSIC} = 1"

        // Tri par titre (ordre alphabétique)
        private const val SONG_SORT_ORDER = "${MediaStore.Audio.Media.TITLE} ASC"
    }

    /**
     * Récupère toutes les chansons du téléphone
     */
    fun getAllSongs(): Flow<List<Song>> = flow {
        val songs = mutableListOf<Song>()

        contentResolver.query(
            AUDIO_URI,
            SONG_PROJECTION,
            SONG_SELECTION,
            null,
            SONG_SORT_ORDER
        )?.use { cursor ->

            // Indices des colonnes
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

            // Parcourir toutes les lignes
            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(AUDIO_URI, id)

                    val song = Song(
                        id = id,
                        title = cursor.getString(titleColumn) ?: "Unknown",
                        artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                        album = cursor.getString(albumColumn) ?: "Unknown Album",
                        albumId = cursor.getLong(albumIdColumn),
                        duration = cursor.getLong(durationColumn),
                        path = cursor.getString(pathColumn) ?: "",
                        uri = uri,
                        size = cursor.getLong(sizeColumn),
                        mimeType = cursor.getString(mimeTypeColumn) ?: "",
                        dateAdded = cursor.getLong(dateAddedColumn),
                        dateModified = cursor.getLong(dateModifiedColumn),
                        track = cursor.getInt(trackColumn),
                        year = cursor.getInt(yearColumn)
                    )

                    songs.add(song)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing song from MediaStore")
                }
            }
        }

        Timber.d("Found ${songs.size} songs in MediaStore")
        emit(songs)
    }.flowOn(Dispatchers.IO)  // Exécuter sur le thread IO

    /**
     * Récupère tous les albums
     */
    fun getAllAlbums(): Flow<List<Album>> = flow {
        val albums = mutableListOf<Album>()

        val albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR
        )

        contentResolver.query(
            albumUri,
            projection,
            null,
            null,
            "${MediaStore.Audio.Albums.ALBUM} ASC"
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val songCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)

            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idColumn)
                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        id
                    )

                    val album = Album(
                        id = id,
                        name = cursor.getString(albumColumn) ?: "Unknown Album",
                        artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                        artistId = 0,  // Sera rempli plus tard si nécessaire
                        songCount = cursor.getInt(songCountColumn),
                        year = cursor.getInt(yearColumn),
                        artworkUri = artworkUri.toString()
                    )

                    albums.add(album)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing album from MediaStore")
                }
            }
        }

        Timber.d("Found ${albums.size} albums in MediaStore")
        emit(albums)
    }.flowOn(Dispatchers.IO)

    /**
     * Récupère tous les artistes
     */
    fun getAllArtists(): Flow<List<Artist>> = flow {
        val artists = mutableListOf<Artist>()

        val artistUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )

        contentResolver.query(
            artistUri,
            projection,
            null,
            null,
            "${MediaStore.Audio.Artists.ARTIST} ASC"
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val albumCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val songCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)

            while (cursor.moveToNext()) {
                try {
                    val artist = Artist(
                        id = cursor.getLong(idColumn),
                        name = cursor.getString(artistColumn) ?: "Unknown Artist",
                        albumCount = cursor.getInt(albumCountColumn),
                        songCount = cursor.getInt(songCountColumn)
                    )

                    artists.add(artist)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing artist from MediaStore")
                }
            }
        }

        Timber.d("Found ${artists.size} artists in MediaStore")
        emit(artists)
    }.flowOn(Dispatchers.IO)
}