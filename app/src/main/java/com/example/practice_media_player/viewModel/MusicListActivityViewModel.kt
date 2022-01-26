package com.example.practice_media_player.viewModel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicListActivityViewModel : ViewModel() {
    val item = MutableLiveData<MediaMetadataCompat>()

    fun onClick(item : MediaMetadataCompat){
        this.item.value = item
    }
}