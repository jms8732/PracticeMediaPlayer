package com.example.practice_media_player.service

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.practice_media_player.MusicLibrary
import com.example.practice_media_player.MusicLibrary.loadMusicList
import com.orhanobut.logger.Logger

class MusicService : MediaBrowserServiceCompat() {
    private var mediaSession: MediaSessionCompat? = null
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
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
            ComponentName(applicationContext, this.javaClass),
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
        return BrowserRoot(MusicLibrary.ROOT_ID, null)
    }


    /**
     * 화면 이동시, 계속 호출 됨 -> if문이 없을 경우, recyclerview의 아이템이 계속 갱신되는 현상 발생
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Logger.e("onLoadChildren")
        if (MusicLibrary.libraries[MusicLibrary.KEY]?.isEmpty() == true) {
            val musics = loadMusicList()
            result.sendResult(musics)
            return
        }

        result.sendResult(null)
    }
}