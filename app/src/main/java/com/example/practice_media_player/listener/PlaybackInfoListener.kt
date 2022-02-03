package com.example.practice_media_player.listener

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.orhanobut.logger.Logger

interface PlaybackInfoListener {
    fun onPlaybackStateChanged(state: PlaybackStateCompat?)
    fun onMetadataChanged(metadata: MediaMetadataCompat?)
    fun onPlayCompleted()
}