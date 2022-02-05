package com.example.practice_media_player.ui.view

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.practice_media_player.R
import com.example.practice_media_player.databinding.ViewBottomControllerBinding
import com.example.practice_media_player.helper.MusicServiceHelper
import com.orhanobut.logger.Logger


class BottomControllerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {
    private var isPlaying = false
    var helper : MusicServiceHelper? = null

    val callback = object : MediaControllerCompat.Callback(){
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                bindUI(it)
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when(state?.state){
                PlaybackStateCompat.STATE_PAUSED ->{
                    isPlaying = false
                }
                PlaybackStateCompat.STATE_PLAYING ->{
                    isPlaying = true
                }
                PlaybackStateCompat.STATE_STOPPED ->{}
            }
            bindController()
        }
    }

    private val binding: ViewBottomControllerBinding by lazy {
        DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_bottom_controller,
            null,
            false
        )
    }

    init {
        initUI()
        addView(binding.root)

    }

    private fun initUI(){
        binding.imageViewPlayPause.setOnClickListener {
            when(isPlaying){
                true -> helper?.mediaController?.pause()
                else -> {
                    helper?.controller?.let {
                        helper?.mediaController?.playFromMediaId(it.metadata.description.mediaId,null)
                    }
                }
            }
        }
    }

    private fun bindController(){
        binding.imageViewPlayPause.setImageResource(when(isPlaying){
            true -> android.R.drawable.ic_media_pause
            else -> android.R.drawable.ic_media_play
        })
    }

    private fun bindUI(data : MediaMetadataCompat){
        data.let {
            Logger.e("data: $it")
            binding.textViewTitle.text = it.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            binding.textViewArtist.text = it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

            val uri = it.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
            Glide.with(binding.imageViewThumbnail)
                .load(uri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.imageViewThumbnail)

        }
    }
}