package com.example.practice_media_player.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practice_media_player.BaseActivity
import com.example.practice_media_player.R
import com.example.practice_media_player.adapter.MusicAdapter
import com.example.practice_media_player.databinding.ActivityMusicListBinding
import com.example.practice_media_player.helper.MusicServiceHelper
import com.example.practice_media_player.viewModel.MusicListActivityViewModel
import com.orhanobut.logger.Logger
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * connect, disconnect은 한 쌍이여야 한다.
 * 현재 구조로는 connect callback 안에 subscribe callback이 있기 때문에
 */
class MusicListActivity : BaseActivity<ActivityMusicListBinding>(), MusicServiceHelper.OnSubscriptionListener {
    override fun layoutIds(): Int = R.layout.activity_music_list
    private lateinit var musicAdapter: MusicAdapter
    private val helper: MusicServiceHelper = get()
    private val vm: MusicListActivityViewModel by viewModel()
    private val permissionLaunch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it == true) {
                initMusicService()
            }
        }

    private val callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

        }

    }

    override fun onChildrenLoaded(
        parentId: String,
        children: MutableList<MediaBrowserCompat.MediaItem>
    ) {
        musicAdapter.submitList(children)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initMusicService()
        } else
            permissionLaunch.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

    }

    private fun initMusicService() {
        initAdapter()
        initObserver()

        helper.run {
            listener = this@MusicListActivity
            connect()
            binding.bottomControllerView.helper = this
        }
    }

    override fun onStart() {
        super.onStart()
        helper.registerCallback(callback)
        helper.registerCallback(binding.bottomControllerView.callback)
    }

    override fun onStop() {
        super.onStop()
        helper.unregisterCallback(callback)
        helper.unregisterCallback(binding.bottomControllerView.callback)
    }

    private fun initObserver() {
        vm.item.observe(this) { click ->
            //음악 목록에서 음악을 클릭
            helper.mediaController?.playFromMediaId(click.description.mediaId, null)

            Intent(this@MusicListActivity, MusicDetailActivity::class.java).run {
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

    override fun onDestroy() {
        super.onDestroy()
        Logger.i("onDestroy")
    }
}