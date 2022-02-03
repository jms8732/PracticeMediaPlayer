package com.example.practice_media_player

import android.content.Context
import android.database.MergeCursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaMetadataCompat

object MusicLibrary {
    val ROOT_ID = "__ROOT__"
    val KEY = "ROOT"
    val libraries = mutableMapOf(KEY to mutableListOf<MediaMetadataCompat>())

    val proj = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION
    )

    val selection =
        MediaStore.Audio.Media.IS_MUSIC + " != 0 and " + MediaStore.Audio.Media.MIME_TYPE + "!='application/ogg'"


    fun findMetaData(mediaId : String?) : MediaMetadataCompat?{
        return mediaId?.let { id ->
            libraries[KEY]?.first { it.description.mediaId == id }
        }
    }

    fun Context.loadMusicList(): MutableList<MediaBrowserCompat.MediaItem> {
        val ret = mutableListOf<MediaBrowserCompat.MediaItem>()
        val inCur = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, selection,
            null, null
        )

        val outCur =
            contentResolver.query(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                proj,
                selection,
                null,
                null
            )

        MergeCursor(arrayOf(inCur, outCur)).run {
            while (moveToNext()) {
                val mediaItem = MediaMetadataCompat.Builder().also {
                    val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val artist = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val album = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))

                    val title =
                        getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val duration = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))

                    it.putString(
                        MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                        Uri.withAppendedPath(
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                            id.toString()
                        ).toString()
                    )
                    it.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id.toString())
                    it.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    it.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    it.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    it.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)

                }.build()

                libraries[KEY]?.add(mediaItem)

                /**
                 * flag에 따라서 Android auto 및 서비스에 표현되는 방식이 달라짐
                 * FLAG_PLAYABLE / FLAG_BROWSABLE
                 */
                ret.add(
                    MediaBrowserCompat.MediaItem(
                        mediaItem.description,
                        FLAG_PLAYABLE
                    )
                )
            }
        }

        return ret
    }
}