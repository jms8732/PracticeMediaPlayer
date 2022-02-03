package com.example.practice_media_player.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.drm.DrmStore
import android.media.MediaDrm
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.example.practice_media_player.R
import com.example.practice_media_player.service.MusicService
import com.example.practice_media_player.ui.MusicListActivity

class MusicNotificationManager(private val context: Context) {
    companion object {
        val NOTIFICATION_ID = 412
        val CHANNEL_ID = "com.example.app.musicplayer.channel"
        val REQUEST_CODE = 501
    }

    private val mPlayAction = NotificationCompat.Action(
        android.R.drawable.ic_media_play,
        "Play",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PLAY
        )
    )

    private val mPauseAction = NotificationCompat.Action(
        android.R.drawable.ic_media_pause,
        "Pause",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PAUSE
        )
    )

    private val mPrevAction = NotificationCompat.Action(
        android.R.drawable.ic_media_previous,
        "Previous",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )
    private val mNextAction = NotificationCompat.Action(
        android.R.drawable.ic_media_next,
        "Next",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        manager.cancelAll()
    }

    fun getNotification(
        metadata: MediaMetadataCompat, state: PlaybackStateCompat,
        token: MediaSessionCompat.Token
    ): Notification {
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        val description = metadata.description

        return buildNotification(state, token, isPlaying, description).build()
    }

    @SuppressLint("ResourceAsColor")
    private fun buildNotification(
        state: PlaybackStateCompat,
        token: MediaSessionCompat.Token,
        isPlaying: Boolean,
        description: MediaDescriptionCompat
    ): NotificationCompat.Builder {
        if (isAndroidOorHigher())
            createChannel()

        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
                .setColor(android.R.color.holo_red_light)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context, PlaybackStateCompat.ACTION_STOP
                    )
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            if ((state.actions.and(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)) != 0L) {
                addAction(mPrevAction)
            }
            addAction(if (isPlaying) mPauseAction else mPlayAction)

            if ((state.actions.and(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)) != 0L) {
                addAction(mNextAction)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val name = "MediaSession"
            val description = "MediaSession and MediaPlayer"
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                setDescription(description)
                enableLights(true)

            }

            manager.createNotificationChannel(mChannel)
        }
    }

    private fun isAndroidOorHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    private fun createContentIntent(): PendingIntent {
        return PendingIntent.getActivity(
            context, REQUEST_CODE,
            Intent(context, MusicListActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}