package com.example.practice_media_player.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.practice_media_player.MusicLibrary
import com.example.practice_media_player.listener.PlaybackInfoListener
import com.example.practice_media_player.ui.MusicListActivity
import java.lang.RuntimeException

/**
 * 미디어를 실행할 때, 추상 클래스를 한번 거쳐서 실행되는 구조
 * 추상 클래스(BasePlayAdapter)에는 Audio에 관련된 로직이 구현
 */
class PlayAdapter(private val listener: PlaybackInfoListener, private val context: Context) :
    BasePlayAdapter(context) {
    private var mediaPlayer: MediaPlayer? = null
    private var mState = 0
    private var completion = false
    private var position: Long = -1
    private var fileChange = false
    private var currentMediaId : String? = null

    override fun onPlay() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    override fun isPlaying(): Boolean = mediaPlayer != null && mediaPlayer?.isPlaying == true

    override fun onPause() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            setNewState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    override fun onStop() {
        setNewState(PlaybackStateCompat.STATE_STOPPED)
        release()
    }

    override fun seekTo(position: Long) {
        mediaPlayer?.let {
            if (!it.isPlaying)
                this.position = position

            it.seekTo(this.position.toInt())

            setNewState(mState)
        }
    }

    override fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    override fun playFromMediaId(mediaId: String?) {
        fileChange = mediaId != currentMediaId || currentMediaId == null

        if(completion){
            fileChange = true
            completion = false
        }

        if(fileChange){
            release()
        }else{
            if(!isPlaying()){
                play()
                return
            }
        }

        currentMediaId = mediaId
        initializePlayer()

        try{
            MusicLibrary.findMetaData(mediaId)?.let {
                val uri = it.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
                mediaPlayer?.setDataSource(context, Uri.parse(uri))

                mediaPlayer?.prepare()
            }
        }catch (e : Exception){
            throw RuntimeException("Failed",e)
        }

        play()
    }

    private fun initializePlayer() {
        if (mediaPlayer == null)
            mediaPlayer = MediaPlayer().apply {
                setOnCompletionListener {
                    listener.onPlayCompleted()
                    setNewState(PlaybackStateCompat.STATE_PAUSED)
                }
            }
    }


    private fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun setNewState(state: Int) {
        mState = state
        if (mState == PlaybackStateCompat.STATE_STOPPED) {
            completion = true
        }

        var reportPosition = 0L

        if(position >= 0){
            reportPosition = position

            if(mState == PlaybackStateCompat.STATE_PLAYING) position = -1
        }else
           reportPosition = mediaPlayer?.run {
                currentPosition.toLong()
            } ?: 0L


        val builder = PlaybackStateCompat.Builder().apply {
            setActions(getActions())
            setState(
                mState,
                reportPosition,
                1.0f,
                SystemClock.elapsedRealtime()
            )
        }.build()


        listener.onPlaybackStateChanged(builder)
    }

    override fun getMediaMetaData(): MediaMetadataCompat? = MusicLibrary.findMetaData(currentMediaId)

    private fun getActions(): Long {
        val actions =
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

        actions or when (mState) {
            PlaybackStateCompat.STATE_PLAYING -> PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SEEK_TO
            PlaybackStateCompat.STATE_STOPPED -> PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY
            PlaybackStateCompat.STATE_PAUSED  -> PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP
            else                              -> PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PLAY_PAUSE
        }
        return actions
    }
}