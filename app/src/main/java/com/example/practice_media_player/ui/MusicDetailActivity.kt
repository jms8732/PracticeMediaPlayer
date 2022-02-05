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
import com.example.practice_media_player.service.MusicService
import com.orhanobut.logger.Logger
import org.koin.android.ext.android.get

class MusicDetailActivity : BaseActivity<ActivityMusicDetailBinding>() {
    override fun layoutIds(): Int = R.layout.activity_music_detail
    private val helper: MusicServiceHelper = get()
    private var isPlaying = false

    private val callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    isPlaying = true
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    isPlaying = false
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    isPlaying = false
                }
            }
            bindController()
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                bindUI(it)
            }
        }
    }

    private fun bindUI(data: MediaMetadataCompat) {
        data.let {
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

    private fun bindController() {
        when (isPlaying) {
            true -> {
                binding.imageViewPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
            else -> {
                binding.imageViewPlayPause.setImageResource(android.R.drawable.ic_media_play)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        initUI()
    }

    private fun initUI() {
        binding.imageViewPlayPause.setOnClickListener {
            Logger.e("isPlaying: $isPlaying")
            when (isPlaying) {
                true -> {
                    helper.mediaController?.pause()
                }
                else -> {
                    helper.controller?.let {
                        helper.mediaController?.playFromMediaId(it.metadata.description.mediaId,null)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        helper.registerCallback(callback)
    }

    override fun onStop() {
        super.onStop()
        helper.unregisterCallback(callback)
    }
}