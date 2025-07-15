package com.apero.testcrawldata.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.apero.testcrawldata.MainActivity
import com.apero.testcrawldata.R
import com.apero.testcrawldata.alarmreceiver.CancelAlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationHelper(private val context: Context) {

    private val channelId = "countdown_channel_id"
    private val channelName = "Countdown Notification"

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun onCancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for countdown timer"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createInitialNotification(
        title: String,
        content: String
    ): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    fun showNotificationRealtime(
        title: String,
        timeStart: Long,
        numberCountDown: StateFlow<Long>,
        notificationId: Int = 1001,
        scope: CoroutineScope
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create cancel action
        val cancelIntent = Intent(context, CancelAlarmReceiver::class.java).apply {
            action = CancelAlarmReceiver.ACTION_CANCEL_ALARM
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scope.launch {
            numberCountDown.collect { count ->
                val minutes = count / 60
                val seconds = count % 60
                val timeText = if (minutes > 0) {
                    String.format("%d:%02d", minutes, seconds)
                } else {
                    String.format("%d giây", seconds)
                }
                
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText("Tắt máy sau: $timeText")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Hủy",
                        cancelPendingIntent
                    )
                    .build()

                notificationManager.notify(notificationId, notification)
            }
        }
    }
}