package com.example.practice_media_player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver

class MusicNotificationManger(private val context: Context) {
    companion object {
        val NOTIFICATION_ID = 400
        val CHANNEL_ID = "test.play"
        val REQUEST_CODE = 201
    }

    private val mPlayAction = NotificationCompat.Action(
        android.R.drawable.ic_media_play,
        "play",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PLAY
        )
    )

    private val mPauseAction = NotificationCompat.Action(
        android.R.drawable.ic_media_pause,
        "pause",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PAUSE
        )
    )

    private val mNextAction = NotificationCompat.Action(
        android.R.drawable.ic_media_next,
        "forward",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )

    private val mPreAction = NotificationCompat.Action(
        android.R.drawable.ic_media_previous,
        "previous",
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        manager.cancelAll()
    }

    fun getNotification(
        metadata : MediaMetadataCompat, state : PlaybackStateCompat,
        token: MediaSessionCompat.Token
    ) : Notification{
        val isPlaying = state.state == PlaybackStateCompat.STATE_PLAYING
        val description = metadata.description

        return buildNotification(state, token, isPlaying, description)
    }

    private fun buildNotification(
        state : PlaybackStateCompat,
        token : MediaSessionCompat.Token,
        isPlaying : Boolean,
        description : MediaDescriptionCompat
    ) : Notification{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel()

        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0,1,2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(createPendingIntent())
                .setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.ic_launcher_foreground))
                .setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,PlaybackStateCompat.ACTION_STOP
                    )
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            if(state.actions.and(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0L)
                addAction(mPreAction)

            addAction(if(isPlaying) mPauseAction else mPlayAction)

            if(state.actions.and(PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0L)
                addAction(mNextAction)

        }.build()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val name = "MediaSession"
            val mChannel = NotificationChannel(
                CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Test"
                enableLights(true)
                enableVibration(false)
                lightColor = Color.RED
            }

            manager.createNotificationChannel(mChannel)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createPendingIntent(): PendingIntent {
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}