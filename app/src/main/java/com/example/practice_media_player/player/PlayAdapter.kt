package com.example.practice_media_player.player

import android.content.Context
import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import com.example.practice_media_player.listener.PlaybackInfoListener

class PlayAdapter(private val listener : PlaybackInfoListener, private val context : Context) : BasePlayAdapter(context) {
    override fun onPlay() {
        TODO("Not yet implemented")
    }

    override fun isPlaying(): Boolean {
        TODO("Not yet implemented")
    }

    override fun onPause() {
        TODO("Not yet implemented")
    }

    override fun onStop() {
        TODO("Not yet implemented")
    }

    override fun seekTo(position: Long) {
        TODO("Not yet implemented")
    }

    override fun setVolume(volume: Float) {
        TODO("Not yet implemented")
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