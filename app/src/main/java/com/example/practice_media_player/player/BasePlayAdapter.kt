package com.example.practice_media_player.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.RequiresApi
import androidx.compose.ui.draw.BuildDrawCacheParams
import androidx.core.content.getSystemService

/**
 * Audio 조절에 관련된 클래스
 */
abstract class BasePlayAdapter(private val applicationContext: Context) {
    abstract fun onPlay()
    abstract fun isPlaying(): Boolean
    abstract fun onPause()
    abstract fun onStop()
    abstract fun seekTo(position: Long)
    abstract fun setVolume(volume: Float)
    abstract fun playFromMediaId(mediaId : String?)
    abstract fun getMediaMetaData() : MediaMetadataCompat?


    private val MEDIA_VOLUME_DEFAULT = 1.0F
    private val MEDIA_VOLUME_DUCK = 2.0F
    private val AUDIO_NOISY_INTENT_FILTER = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val mAudioManger: AudioManager =
        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var mPlayOnAudioFocus = false
    private var mAudioNoisyReceiveRegistered = false

    private val audioNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                if (isPlaying())
                    pause()
        }
    }

    private val mAudioHelper = AudioHelper()

    fun play(){
        if(!mPlayOnAudioFocus){
            mAudioHelper.requestAudioFocus()
            registerAudioNoisyReceiver()
            onPlay()
        }
    }

    fun pause(){
        if(mPlayOnAudioFocus){
           mAudioHelper.abandonAudioFocus()
        }
        unregisterAudioNoisyReceiver()
        onPause()
    }

    fun stop(){
        mAudioHelper.abandonAudioFocus()
        unregisterAudioNoisyReceiver()
        onStop()
    }

    private fun registerAudioNoisyReceiver(){
        if(!mAudioNoisyReceiveRegistered){
            applicationContext.registerReceiver(audioNoisyReceiver,AUDIO_NOISY_INTENT_FILTER)
            mAudioNoisyReceiveRegistered = true
        }
    }

    private fun unregisterAudioNoisyReceiver(){
        if(mAudioNoisyReceiveRegistered) {
            applicationContext.unregisterReceiver(audioNoisyReceiver)
            mAudioNoisyReceiveRegistered = false
        }
    }

    private inner class AudioHelper : AudioManager.OnAudioFocusChangeListener {
        private lateinit var afr: AudioFocusRequest

        fun requestAudioFocus(): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                afr = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .build()

                return mAudioManger.requestAudioFocus(afr) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                return mAudioManger.requestAudioFocus(
                    this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        }

        fun abandonAudioFocus() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mAudioManger.abandonAudioFocusRequest(afr)
            } else
                mAudioManger.abandonAudioFocus(this)
        }

        override fun onAudioFocusChange(focusChange: Int) {
            when(focusChange){
                AudioManager.AUDIOFOCUS_GAIN ->{
                    if(mPlayOnAudioFocus && !isPlaying())
                        play()
                    else if(isPlaying()){
                        setVolume(MEDIA_VOLUME_DEFAULT)
                    }
                    mPlayOnAudioFocus = false
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->{
                    setVolume(MEDIA_VOLUME_DUCK)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->{
                    if(isPlaying()){
                        mPlayOnAudioFocus = true
                        pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS ->{
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        mAudioManger.abandonAudioFocusRequest(afr)
                    else
                        mAudioManger.abandonAudioFocus(this)
                    mPlayOnAudioFocus = false
                    stop()
                }
            }
        }

    }
}