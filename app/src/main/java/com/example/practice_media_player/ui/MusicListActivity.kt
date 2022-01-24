package com.example.practice_media_player.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practice_media_player.BaseActivity
import com.example.practice_media_player.R
import com.example.practice_media_player.adapter.MusicAdapter
import com.example.practice_media_player.databinding.ActivityMusicListBinding
import com.example.practice_media_player.helper.MusicServiceHelper

class MusicListActivity : BaseActivity<ActivityMusicListBinding>() {
    override fun layoutIds(): Int = R.layout.activity_music_list
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var musicHelper: MusicServiceHelper
    private val permissionLaunch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if(it == true){
                initMusicService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        permissionLaunch.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        initMusicService()
    }

    private fun initMusicService(){
        musicHelper = MediaConnection(applicationContext)
        initAdapter()
    }

    override fun onStart() {
        super.onStart()
        musicHelper.connect()
    }

    override fun onStop() {
        super.onStop()
        musicHelper.disconnect()
    }


    private fun initAdapter() {
        binding.musicRecycleView.run {
            musicAdapter = MusicAdapter()
            adapter = musicAdapter

            layoutManager = LinearLayoutManager(this@MusicListActivity)
        }
    }

    inner class MediaConnection(context: Context) : MusicServiceHelper(context) {
        override fun onChildLoaded(children: MutableList<MediaBrowserCompat.MediaItem>) {
            musicAdapter.submitList(children)
        }
    }
}