package com.example.practice_media_player

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practice_media_player.databinding.ActivityMainBinding

class MainActivity : BindingActivity<ActivityMainBinding>() {
    override fun getLayoutIds(): Int = R.layout.activity_main
    private lateinit var helper: MediaHelper
    private lateinit var adapter : MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)

        binding.musicRecycleView.run {
            layoutManager = LinearLayoutManager(this@MainActivity)

            this@MainActivity.adapter = MusicAdapter()
            adapter = this@MainActivity.adapter
        }
    }

    override fun onStart() {
        super.onStart()
        helper.onStart()
    }

    override fun onStop() {
        super.onStop()
        helper.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            helper = MediaConnection()
    }

    private inner class MediaConnection : MediaHelper(this){
        override fun onChildrenLoaded(children: MutableList<MediaBrowserCompat.MediaItem>) {
            adapter.submitList(children)
        }
    }
}