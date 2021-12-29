package com.example.practice_media_player

import android.media.session.PlaybackState
import android.support.v4.media.session.PlaybackStateCompat

interface PlaybackStateListener {
    fun onPlaybackStateChange(state : PlaybackStateCompat)
    fun onPlayComplete()
}