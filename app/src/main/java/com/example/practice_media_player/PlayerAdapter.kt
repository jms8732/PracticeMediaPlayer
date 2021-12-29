package com.example.practice_media_player

import android.content.Context
import android.media.MediaPlayer
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import java.lang.Exception
import java.lang.RuntimeException

class PlayerAdapter(private val context : Context, private val listener : PlaybackStateListener) {
    private var mSeekWhileNotPlaying = -1
    private var mCurrentMediaPlayedToCompletion = false
    private var mState = 0
    private var mFilename = ""
    private var mMediaPlayer: MediaPlayer? = null
    private var mCurrentMedia: MediaMetadataCompat? = null



    private fun initializeMediaPlayer(){
        if(mMediaPlayer == null){
            mMediaPlayer = MediaPlayer().apply {
                setOnCompletionListener {
                    setNewState(PlaybackStateCompat.STATE_PAUSED)
                }
            }
        }
    }

    fun getMediaCompat() = mCurrentMedia

    fun playFile(filename: String) {
        var mediaChanged = (mFilename == null || filename != mFilename)

        if (mCurrentMediaPlayedToCompletion) {
            mediaChanged = true
            mCurrentMediaPlayedToCompletion = false
        }

        if (!mediaChanged) {
            if (!isPlaying()) {
                play()
            }
            return
        } else
            release()

        mFilename = filename
        initializeMediaPlayer()

        try {
            //디바이스 내에 파일을 읽을 때, 사용
            val assetFileDescriptor = context.assets.openFd(mFilename)
            mMediaPlayer?.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to open file: $mFilename", e)
        }

        try {
            mMediaPlayer?.prepare()
        } catch (e: Exception) {
            throw RuntimeException("Failed to open file: $mFilename", e)
        }

        play()
    }

    private fun play() {
        if(mMediaPlayer != null && mMediaPlayer?.isPlaying == false){
            mMediaPlayer?.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    fun pause(){
        if(mMediaPlayer != null && mMediaPlayer?.isPlaying == true){
            mMediaPlayer?.pause()
            setNewState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    private fun setNewState(state : Int){
        mState = state
        if (mState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true
        }

        var reportPosition = 0

        if (mSeekWhileNotPlaying >= 0) {
            reportPosition = mSeekWhileNotPlaying

            if (mState == PlaybackStateCompat.STATE_PLAYING) mSeekWhileNotPlaying = -1
        } else
            reportPosition = mMediaPlayer?.run { currentPosition } ?: 0

        val builder = PlaybackStateCompat.Builder().apply {
            setActions(getAvailableActions())
            setState(
                mState,
                reportPosition.toLong(),
                1.0f,
                SystemClock.elapsedRealtime()
            )
        }

        listener.onPlaybackStateChange(builder.build())
    }

    private fun release() {
        if (mMediaPlayer != null) {
            mMediaPlayer?.release()
            mMediaPlayer = null
        }
    }

    fun onStop(){
        setNewState(PlaybackStateCompat.STATE_STOPPED)
        release()
    }

    fun isPlaying(): Boolean = mMediaPlayer != null && mMediaPlayer?.isPlaying == true

    private fun getAvailableActions(): Long {
        var actions =
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

        actions = when (mState) {
            PlaybackStateCompat.STATE_STOPPED -> actions or PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE
            PlaybackStateCompat.STATE_PLAYING -> actions or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO
            PlaybackStateCompat.STATE_PAUSED  -> actions or PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP
            else                              -> actions or PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PAUSE
        }

        return actions
    }
    fun setVolume(volume: Float) {
        mMediaPlayer?.run {
            setVolume(volume, volume)
        }
    }
}