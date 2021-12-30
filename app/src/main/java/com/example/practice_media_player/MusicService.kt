package com.example.practice_media_player

import android.content.Intent
import android.database.MergeCursor
import android.media.browse.MediaBrowser.MediaItem.FLAG_PLAYABLE
import android.media.session.MediaSession
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.orhanobut.logger.Logger
import java.util.concurrent.TimeUnit

class MusicService : MediaBrowserServiceCompat() {
    private val ROOT = "ROOT"
    private lateinit var mSession : MediaSessionCompat
    private val adapter = PlayerAdapter(this,Listener())


    override fun onCreate() {
        super.onCreate()
        mSession = MediaSessionCompat(this,"CustomMusicService").apply {
            setCallback(MediaSessionCallback())
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setSessionToken(sessionToken)

        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(ROOT,null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        //미디어 데이터 가져온다
        Log.d(TAG, "onLoadChildren: ")
        result.sendResult(onLoadMediaItems())
    }

    private fun onLoadMediaItems() : MutableList<MediaBrowserCompat.MediaItem>{
        //content resolver를 통해서 음악 데이터를 가져온다.

        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
        )

        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"


        val int_cursor = contentResolver.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,proj,selection,null,null)
        val ext_cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,proj,selection,null,null)

        val temp = mutableListOf<MediaBrowserCompat.MediaItem>()
        with(MergeCursor(arrayOf(int_cursor,ext_cursor))){
            while (moveToNext()){
                val artist = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val album = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val duration = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                
                val description = MediaMetadataCompat.Builder().apply { 
                    putLong(MediaMetadataCompat.METADATA_KEY_DURATION,duration)
                    putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artist)
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM,album)
                    putString(MediaMetadataCompat.METADATA_KEY_TITLE,title)
                    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,id.toString())
                }.build().description

                temp.add(MediaBrowserCompat.MediaItem(description,MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
            }
        }

        return temp
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    private inner class MediaSessionCallback  : MediaSessionCompat.Callback(){
        private val mPlayList = ArrayList<MediaSessionCompat.QueueItem>()
        private var mQueueIndex =- 1
        private var mPreparedMedia : MediaMetadataCompat?= null


        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            mPlayList.add(MediaSessionCompat.QueueItem(description,description.hashCode().toLong()))
            mQueueIndex = if(mQueueIndex == -1) 0 else mQueueIndex
            mSession.setQueue(mPlayList)
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            mPlayList.remove(MediaSessionCompat.QueueItem(description,description.hashCode().toLong()))
            mQueueIndex = if(mPlayList.isEmpty()) -1 else mQueueIndex
            mSession.setQueue(mPlayList)
        }

        override fun onPrepare() {
            if(mQueueIndex < 0 && mPlayList.isEmpty()) return

            val id = mPlayList[mQueueIndex].description.mediaId
            mPreparedMedia = getMetadata(id)
            mSession.setMetadata(mPreparedMedia)

            if(!mSession.isActive) mSession.isActive = true

            Logger.e("onPrepare: $mPreparedMedia")
        }

        override fun onPlay() {
            if(!isReadyToPlay()) return
            Logger.e("onPlay")

            mPreparedMedia?.run {
                val id = this.description.mediaId
                val meta = getMetadata(id)

                adapter.playFile(meta.description.title.toString())
            } ?: onPrepare()
        }

        override fun onStop() {
            adapter.onStop()
            mSession.isActive = false
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
        }

        override fun onPause() {
            adapter.pause()
        }

        override fun onSeekTo(pos: Long) {

        }

        override fun onSkipToNext() {
            mQueueIndex = (++mQueueIndex % mPlayList.size)
            mPreparedMedia = null
            onPlay()
        }

        override fun onSkipToPrevious() {
            mQueueIndex = if(mQueueIndex >0 ) mQueueIndex -1 else mPlayList.size-1
            mPreparedMedia = null
            onPlay()
        }

        private fun isReadyToPlay() = mPlayList.isNotEmpty()
    }

    inner class Listener : PlaybackStateListener{
        override fun onPlaybackStateChange(state: PlaybackStateCompat) {
            Logger.e("state: $state")
        }

        override fun onPlayComplete() {

        }
    }
}