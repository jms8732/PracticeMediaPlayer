package com.example.practice_media_player.helper

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.practice_media_player.service.MusicService

/**
 * 순서
 * 1. MediaBrowser 생성
 * 2. 연결되면 MediaController
 * 3. MediaBrowser subscribe -> 구독이 완료되면 onChildrenLoaded 메소드에서 미디어 리스트들이 내려온다.
 *
 */
abstract class MusicServiceHelper(context: Context) {
    private var mediaBrowser: MediaBrowserCompat? = null
    var mediaController: MediaControllerCompat? = null
    var mediaTransportController: MediaControllerCompat.TransportControls? = null

    abstract fun onChildLoaded(
        children: MutableList<MediaBrowserCompat.MediaItem>
    )

    abstract fun onPlaybackStateChanged(state: PlaybackStateCompat?)
    abstract fun onMetadataChanged(metaData : MediaMetadataCompat?)

    fun connect() {
        mediaBrowser?.connect()
    }

    fun disconnect() {
        mediaBrowser?.disconnect()
    }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            this@MusicServiceHelper.onPlaybackStateChanged(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            this@MusicServiceHelper.onMetadataChanged(metadata)
        }
    }

    private val mediaBrowserSubscriptionCallback =
        object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                onChildLoaded(children)
            }
        }


    private val mediaBrowserCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mediaBrowser?.let {
                mediaController = MediaControllerCompat(context, it.sessionToken).apply {
                    this@MusicServiceHelper.onMetadataChanged(this.metadata)
                    this@MusicServiceHelper.onPlaybackStateChanged(this.playbackState)

                    registerCallback(mediaControllerCallback)
                }
                mediaTransportController = mediaController?.transportControls

                it.subscribe(it.root, mediaBrowserSubscriptionCallback)
            }
        }

    }

    init {
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MusicService::class.java),
            mediaBrowserCallback,
            null
        )
    }
}