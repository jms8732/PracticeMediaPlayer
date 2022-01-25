package com.example.practice_media_player.ui

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import com.bumptech.glide.Glide
import com.example.practice_media_player.BaseActivity
import com.example.practice_media_player.R
import com.example.practice_media_player.databinding.ActivityMusicDetailBinding

class MusicDetailActivity : BaseActivity<ActivityMusicDetailBinding>() {
    override fun layoutIds(): Int = R.layout.activity_music_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        initUI()
    }

    private fun initUI(){
        val music = intent.getParcelableExtra<MediaMetadataCompat>("data")
        music?.let {
            binding.textViewTitle.text = it.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            binding.textViewArtist.text = it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

            val uri = it.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            Glide.with(binding.imageViewAlbumArt)
                .load(uri)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.imageViewAlbumArt)
        }
    }
}