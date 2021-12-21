package com.example.practice_media_player

import android.content.Context
import android.database.MergeCursor
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat

fun Context.getMetadata(id: String?): MediaMetadataCompat {


    val proj = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION
    )

    val selection = MediaStore.Audio.Media._ID + " == ?"

    val int_cur = contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, selection,
        arrayOf(id), null
    )

    val out_cur =
        contentResolver.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, proj, selection, arrayOf(id), null)

    return MergeCursor(arrayOf(int_cur, out_cur)).run {
        MediaMetadataCompat.Builder().also {
            while (moveToNext()) {
                val artist = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val album = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val duration = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))


                it.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id.toString())
                it.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                it.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                it.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                it.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)

            }
        }.build()
    }
}