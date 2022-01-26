package com.example.practice_media_player.player

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import com.example.practice_media_player.listener.PlaybackInfoListener

class PlayAdapter(private val listener : PlaybackInfoListener) {

    fun play(){

        setMetaData()
    }

    fun pause(){

    }

    fun stop(){

    }

    fun skipNext(){

    }

    fun skipPrevious(){

    }

    private fun setMetaData(){

        val builder = PlaybackStateCompat.Builder().apply {
            setActions()
            setState(
                PlaybackStateCompat.STATE_PLAYING,
                10,
                1.0f,
                SystemClock.elapsedRealtime()
            )
        }
        listener.onMetadataChanged()
    }
}