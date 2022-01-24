package com.example.practice_media_player.service

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.practice_media_player.MusicLibrary
import com.example.practice_media_player.MusicLibrary.loadMusicList

class MusicService : MediaBrowserServiceCompat() {
    private var mediaSession : MediaSessionCompat? = null
    private val mediaSessionCallback = object : MediaSessionCompat.Callback(){
        override fun onAddQueueItem(description: MediaDescriptionCompat?, index: Int) {
            super.onAddQueueItem(description, index)
        }

        override fun onPlay() {
            super.onPlay()
        }

        override fun onStop() {
            super.onStop()
        }
    }


    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(
            applicationContext,
            "MusicService",
            ComponentName(applicationContext,this.javaClass),
            null
        ).apply {
            setCallback(mediaSessionCallback)
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setSessionToken(sessionToken)
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MusicLibrary.ROOT_ID,null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        val musics = loadMusicList()
        result.sendResult(musics)
    }
}