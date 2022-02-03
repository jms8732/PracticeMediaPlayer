package com.example.practice_media_player.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practice_media_player.BaseActivity
import com.example.practice_media_player.R
import com.example.practice_media_player.adapter.MusicAdapter
import com.example.practice_media_player.databinding.ActivityMusicListBinding
import com.example.practice_media_player.helper.MusicServiceHelper
import com.example.practice_media_player.viewModel.MusicListActivityViewModel
import com.orhanobut.logger.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel

class MusicListActivity : BaseActivity<ActivityMusicListBinding>() {
    override fun layoutIds(): Int = R.layout.activity_music_list
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var musicHelper: MusicServiceHelper
    private val vm : MusicListActivityViewModel by viewModel()
    private val permissionLaunch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if(it == true){
                initMusicService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            initMusicService()
        }else
            permissionLaunch.launch(Manifest.permission.READ_EXTERNAL_STORAGE)


    }

    private fun initMusicService(){
        musicHelper = MediaConnection(applicationContext)
        initAdapter()
        initObserver()
    }

    override fun onStart() {
        super.onStart()
        musicHelper.connect()
    }

    override fun onStop() {
        super.onStop()
        musicHelper.disconnect()
    }

    private fun initObserver(){
        vm.item.observe(this){
            //음악 목록에서 음악을 클릭
            musicHelper.mediaTransportController?.play()

            Intent(this@MusicListActivity, MusicDetailActivity::class.java).run {
                putExtra("data",it)
                startActivity(this)
            }
        }
    }


    private fun initAdapter() {
        binding.musicRecycleView.run {
            musicAdapter = MusicAdapter(vm)
            adapter = musicAdapter

            layoutManager = LinearLayoutManager(this@MusicListActivity)
        }
    }

    inner class MediaConnection(context: Context) : MusicServiceHelper(context) {
        override fun onChildLoaded(children: MutableList<MediaBrowserCompat.MediaItem>) {
            musicAdapter.submitList(children)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Logger.i("onPlaybackStateChanged")
        }
    }
}