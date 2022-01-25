package com.example.practice_media_player.adapter

import android.content.Intent
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.practice_media_player.BaseViewHolder
import com.example.practice_media_player.MusicLibrary
import com.example.practice_media_player.R
import com.example.practice_media_player.databinding.CellMusicBinding
import com.example.practice_media_player.ui.MusicDetailActivity
import com.orhanobut.logger.Logger

class MusicAdapter : ListAdapter<MediaBrowserCompat.MediaItem, MusicAdapter.MusicViewHolder>(
    diff
) {
    companion object {
        val diff = object : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {
            override fun areItemsTheSame(
                oldItem: MediaBrowserCompat.MediaItem,
                newItem: MediaBrowserCompat.MediaItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: MediaBrowserCompat.MediaItem,
                newItem: MediaBrowserCompat.MediaItem
            ): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.cell_music, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MusicViewHolder(view: View) : BaseViewHolder<CellMusicBinding>(view) {
        init{
            view.rootView.setOnClickListener {
                val item = getItem(adapterPosition).run {
                        MusicLibrary.libraries[MusicLibrary.KEY]?.first { it.description.mediaId == description.mediaId }
                }

                Intent(view.context,MusicDetailActivity::class.java).let {
                    it.putExtra("data",item)
                    view.context.startActivity(it)
                }
            }
        }

        fun bind(position: Int) {
            binding.let {
                val item = getItem(position).run {
                    MusicLibrary.libraries[MusicLibrary.KEY]?.first { it.description.mediaId == description.mediaId }
                }

                item?.let {
                    binding.title.text = it.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                    binding.artist.text = it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

                    val uri = it.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
                    Logger.i("uri: $uri")
                    Glide.with(binding.imageView)
                        .load(uri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.imageView)
                }

                binding.executePendingBindings()
            }
        }
    }
}