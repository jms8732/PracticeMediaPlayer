package com.example.practice_media_player.adapter

import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.practice_media_player.BaseViewHolder
import com.example.practice_media_player.R
import com.example.practice_media_player.databinding.CellMusicBinding
import com.orhanobut.logger.Logger

class MusicAdapter : ListAdapter<MediaBrowserCompat.MediaItem,MusicAdapter.MusicViewHolder>(
    diff
) {
    companion object{
        val diff = object : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>(){
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
        return MusicViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_music,parent,false))
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MusicViewHolder(view : View) : BaseViewHolder<CellMusicBinding>(view){
        fun bind(position : Int){
            binding.let {
                val item = getItem(position)
                item.description.run {
                    binding.title.text = title
                }
                binding.executePendingBindings()
            }
        }
    }
}