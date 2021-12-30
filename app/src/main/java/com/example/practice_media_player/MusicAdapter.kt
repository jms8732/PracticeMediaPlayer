package com.example.practice_media_player

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.practice_media_player.databinding.CellMusicBinding
import com.orhanobut.logger.Logger

class MusicAdapter(private val helper : MediaHelper) : ListAdapter<MediaBrowserCompat.MediaItem,MusicAdapter.MusicViewHolder>(
    diff
){

    companion object{
        val diff = object : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>(){
            override fun areItemsTheSame(
                oldItem: MediaBrowserCompat.MediaItem,
                newItem: MediaBrowserCompat.MediaItem
            ): Boolean {
                return oldItem.description.mediaId == newItem.description.mediaId
            }

            override fun areContentsTheSame(
                oldItem: MediaBrowserCompat.MediaItem,
                newItem: MediaBrowserCompat.MediaItem
            ): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cell_music,parent,false))
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MusicViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val binding = DataBindingUtil.bind<CellMusicBinding>(view)!!

        init {
            binding.root.setOnClickListener {
                val item = getItem(adapterPosition)
                helper.onPrepared()
                helper.onPlay(item)
            }
        }

        fun bind(position: Int){
            val meta = binding.root.context.getMetadata(getItem(position).mediaId)
            with(meta) {
                binding.title.text = getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                binding.artist.text = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            }
        }
    }
}