package com.example.practice_media_player.ui

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.practice_media_player.BaseActivity
import com.example.practice_media_player.R
import com.example.practice_media_player.databinding.ActivityMusicDetailBinding
import com.example.practice_media_player.helper.MusicServiceHelper
import com.orhanobut.logger.Logger

class MusicDetailActivity : BaseActivity<ActivityMusicDetailBinding>() {
    override fun layoutIds(): Int = R.layout.activity_music_detail
    private lateinit var musicHelper : MusicServiceHelper
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        initUI()
        initSetting()
    }

    private fun initUI(){
       binding.imageViewPlayPause.setOnClickListener {
           Logger.i("isPlaying: $isPlaying")
           when(isPlaying){
               true -> musicHelper.mediaTransportController?.pause()
               else -> {
                   musicHelper.mediaController?.let {
                       musicHelper.mediaTransportController?.playFromMediaId(it.metadata.description.mediaId,null)
                   }
               }
           }
       }
    }

    private fun initSetting(){
        musicHelper = MusicConnection()
    }

    override fun onStart() {
        super.onStart()
        Logger.i("Detail Helper: $musicHelper")
        musicHelper.connect()
    }

    private inner class MusicConnection : MusicServiceHelper(applicationContext){
        override fun onChildLoaded(children: MutableList<MediaBrowserCompat.MediaItem>) {
            Logger.i("on load")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when(state?.state){
                PlaybackStateCompat.STATE_PLAYING ->{
                    binding.imageViewPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                }
                PlaybackStateCompat.STATE_PAUSED ->{
                    binding.imageViewPlayPause.setImageResource(android.R.drawable.ic_media_play)
                }
                PlaybackStateCompat.STATE_STOPPED ->{
                    binding.imageViewPlayPause.setImageResource(android.R.drawable.ic_media_play)
                }
            }

            isPlaying = state?.state == PlaybackStateCompat.STATE_PLAYING
            Logger.e("isPlaying: $isPlaying")
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                binding.textViewTitle.text = it.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                binding.textViewArtist.text = it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

                val uri = it.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
                Glide.with(binding.imageViewAlbumArt)
                    .load(uri)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(binding.imageViewAlbumArt)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        musicHelper.disconnect()
    }
}