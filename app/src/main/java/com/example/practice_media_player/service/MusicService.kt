package com.example.practice_media_player.service

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.practice_media_player.MusicLibrary
import com.example.practice_media_player.MusicLibrary.loadMusicList
import com.example.practice_media_player.listener.PlaybackInfoListener
import com.example.practice_media_player.player.PlayAdapter
import com.orhanobut.logger.Logger

class MusicService : MediaBrowserServiceCompat() {
    private var mediaSession: MediaSessionCompat? = null
    /**
     * MediaSession callback을 통해서 Media Controller 된 정보들을 받는다. (ex. 음악 실행 / 음악 일시 중지 / 다음 음악 / 이전 음악)
     */
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        private var mQueueIndex = -1
        private val mPlayList = ArrayList<MediaSessionCompat.QueueItem>()
        private var mPreparedItem : MediaMetadataCompat?= null

        override fun onAddQueueItem(description: MediaDescriptionCompat?, index: Int) {
            mPlayList.add(MediaSessionCompat.QueueItem(description,description.hashCode().toLong()))

        }

        override fun onPrepare() {
            super.onPrepare()

        }

        override fun onPlay() {
            super.onPlay()
            playback.play()
        }

        override fun onStop() {
            super.onStop()
            playback.stop()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
        }

        override fun onPause() {
            super.onPause()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
        }

    }

    private val listener = object : PlaybackInfoListener{
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            mediaSession?.setPlaybackState(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaSession?.setMetadata(metadata)
        }
    }
    private val playback = PlayAdapter(listener)

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

    /**
     * OnGetRoot 에서 현재 디바이스 설정을 확인할 수 있다. (ex. auto / device)
     * BrowserRoot 에서 설정한 키값을 인자로 보낸다.
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MusicLibrary.ROOT_ID, null)
    }


    /**
     * 화면 이동시, 계속 호출 됨 -> if문이 없을 경우, recyclerview의 아이템이 계속 갱신되는 현상 발생
     * parentId를 이용하여 현재 key값에 맞춰서 데이터를 내려보낸다.
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