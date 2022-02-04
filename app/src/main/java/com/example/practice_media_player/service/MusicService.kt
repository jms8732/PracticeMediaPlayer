package com.example.practice_media_player.service

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.practice_media_player.MusicLibrary
import com.example.practice_media_player.MusicLibrary.findMetaData
import com.example.practice_media_player.MusicLibrary.loadMusicList
import com.example.practice_media_player.listener.PlaybackInfoListener
import com.example.practice_media_player.notification.MusicNotificationManager
import com.example.practice_media_player.player.PlayAdapter
import com.orhanobut.logger.Logger


/**
 * 서비스는 싱글톤이다.
 * onCreate 호출 이후, 다시 호출되지 않는다.(앱이 완전 종료되는 경우를 제외하곤)
 *
 */

class MusicService : MediaBrowserServiceCompat() {
    private var mediaSession: MediaSessionCompat? = null
    private var musics: MutableList<MediaBrowserCompat.MediaItem>? = null
    private val manager: MusicNotificationManager by lazy {
        MusicNotificationManager(this)
    }

    private var mServiceInStartedState = false

    /**
     * MediaSession callback을 통해서 Media Controller 된 정보들을 받는다. (ex. 음악 실행 / 음악 일시 중지 / 다음 음악 / 이전 음악)
     */
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        private var mQueueIndex = -1

        override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
            Logger.i("onPrepareFromMediaId: $mediaId")

            if (mQueueIndex < 0 && isReadyToPlay()) return

            findMetaData(mediaId)?.let {
                mediaSession?.setMetadata(it)

                if (mediaSession?.isActive == false)
                    mediaSession?.isActive = true

            }
        }

        override fun onStop() {
            Logger.i("onStop")
            playback.stop()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            Logger.i("onPlay")
            mQueueIndex = musics?.indexOfFirst { it.description.mediaId == mediaId } ?: -1
            Logger.i("size: ${musics?.size} QueueIndex: $mQueueIndex")

            if (!isReadyToPlay()) return

            onPrepareFromMediaId(mediaId, extras)
            playback.playFromMediaId(mediaId)
        }

        override fun onPause() {
            Logger.i("onPause")
            playback.pause()
        }

        override fun onSkipToNext() {
        }

        override fun onSkipToPrevious() {

        }

        private fun isReadyToPlay() = musics?.isNotEmpty() ?: false
    }

    private val listener = object : PlaybackInfoListener {
        private val serviceManager = NotificationServiceManager()

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            mediaSession?.setPlaybackState(state)

            state?.let {
                when (it.state) {
                    PlaybackStateCompat.STATE_PLAYING -> serviceManager.moveServiceToStatedState(it)
                    PlaybackStateCompat.STATE_PAUSED  -> serviceManager.updateNotificationForPause(it)
                    PlaybackStateCompat.STATE_STOPPED -> serviceManager.moveServiceOutStartedState(it)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaSession?.setMetadata(metadata)
        }

        override fun onPlayCompleted() {
            Logger.i("complete")
        }
    }
    private val playback by lazy { PlayAdapter(listener, applicationContext) }
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
        Logger.e("Service Create!!")
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
     *
     *
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Logger.e("onLoadChildren")
        musics = if (MusicLibrary.libraries[MusicLibrary.KEY]?.isEmpty() == true) {
            loadMusicList()
        } else {
            MusicLibrary.libraries[MusicLibrary.KEY]?.map {
                MediaBrowserCompat.MediaItem(it.description, FLAG_PLAYABLE)
            }?.toMutableList()
        }

        result.sendResult(musics)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.e("onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    private inner class NotificationServiceManager {
        fun moveServiceToStatedState(state: PlaybackStateCompat) {
            val item = playback.getMediaMetaData()
            val notification = manager.getNotification(
                item!!, state, sessionToken!!
            )

            if (!mServiceInStartedState) {
                ContextCompat.startForegroundService(
                    this@MusicService,
                    Intent(this@MusicService, MusicService::class.java)
                )
                mServiceInStartedState  = true
            }
            startForeground(MusicNotificationManager.NOTIFICATION_ID, notification)
        }

        fun updateNotificationForPause(state: PlaybackStateCompat) {
            stopForeground(false)

            val item = playback.getMediaMetaData()
            val notification = manager.getNotification(
                item!!, state, sessionToken!!
            )
            manager.manager.notify(
                MusicNotificationManager.NOTIFICATION_ID,
                notification
            )
        }

        fun moveServiceOutStartedState(state: PlaybackStateCompat) {
            stopForeground(true)
            stopSelf()
            mServiceInStartedState = false
        }

    }
}