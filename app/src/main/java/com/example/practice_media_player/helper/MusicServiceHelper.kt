package com.example.practice_media_player.helper

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.practice_media_player.MusicLibrary
import com.example.practice_media_player.service.MusicService
import com.orhanobut.logger.Logger

class MusicServiceHelper(private val context: Context) {
    private var browser: MediaBrowserCompat? = null
    var controller: MediaControllerCompat? = null
    var mediaController: MediaControllerCompat.TransportControls? = null
        get() = controller?.transportControls

    private val callbacks = mutableListOf<MediaControllerCompat.Callback>()
    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            browser?.let {
                MediaControllerCompat(context, it.sessionToken).apply {
                    registerCallback(controllerCallback)
                    controller = this
                }

                it.subscribe(MusicLibrary.ROOT_ID, subscriptionCallback)
            }
        }
    }

    interface OnSubscriptionListener {
        fun onChildrenLoaded(parentId : String, children : MutableList<MediaBrowserCompat.MediaItem>)
    }

    var listener : OnSubscriptionListener? = null

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            callbacks.forEach {
                it.onPlaybackStateChanged(state)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            callbacks.forEach {
                it.onMetadataChanged(metadata)
            }
        }
    }

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            listener?.onChildrenLoaded(parentId, children)
        }
    }

    fun connect() {
        browser?.connect()
    }

    fun disconnect() {
        browser?.disconnect()
    }

    init {
        browser = MediaBrowserCompat(
            context,
            ComponentName(context, MusicService::class.java),
            connectionCallback,
            null
        )
    }

    fun registerCallback(callback: MediaControllerCompat.Callback) {
        callbacks.add(callback)

        //등록하면 현재 상태를 전달
        controller?.let {
            callback.onMetadataChanged(it.metadata)
            callback.onPlaybackStateChanged(it.playbackState)
        }
    }

    fun unregisterCallback(callback: MediaControllerCompat.Callback) {
        callbacks.remove(callback)
    }
}