package com.example.practice_media_player

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.orhanobut.logger.Logger
import kotlin.math.log

abstract class MediaHelper(private val context: Context) {
    private var mediaBrowser: MediaBrowserCompat? = null
    private var mediaController: MediaControllerCompat? = null


    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            Logger.e("playbackstate change")
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            Logger.e("meta: $metadata")
        }
    }


    private inner class MediaBrowserConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.d(TAG, "onConnected: ")
            mediaController = MediaControllerCompat(context, mediaBrowser?.sessionToken!!).apply {
                registerCallback(MediaControllerCallback())
            }

            mediaBrowser?.subscribe(mediaBrowser?.root!!, MediaBrowserSubscribeCallback())
        }

        override fun onConnectionFailed() {
            Log.d(TAG, "onConnectionFailed: ")
        }
    }

    private inner class MediaBrowserSubscribeCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
            super.onChildrenLoaded(parentId, children)
            this@MediaHelper.onChildrenLoaded(children)
        }
    }

    protected open fun onChildrenLoaded(children: MutableList<MediaBrowserCompat.MediaItem>) {}

    fun getTransportControls() = mediaController?.transportControls
    fun getMediaControllers() = mediaController


    fun onPlay(meta : MediaBrowserCompat.MediaItem){
        Logger.e("Meta: $meta")
        mediaController?.transportControls?.play()
    }

    init {
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MusicService::class.java),
            MediaBrowserConnectionCallback(),
            null
        )
        Logger.e("mediaBrowser init")
    }

    fun onStart(){
        mediaBrowser?.connect()
    }

    fun onStop(){
        mediaBrowser?.disconnect()
    }
}