package com.apero.testcrawldata.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.apero.testcrawldata.DI.DIContainer.alarmRepository
import com.apero.testcrawldata.alarmreceiver.repository.AlarmRepository
import com.apero.testcrawldata.alarmreceiver.repository.AlarmRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class CountdownService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    inner class LocalBinder : Binder() {
        fun getService(): CountdownService = this@CountdownService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        // Gọi startForeground ngay khi service bắt đầu
        val initialNotification = notificationHelper.createInitialNotification(
            title = "Countdown Service",
            content = "Đang bắt đầu đếm ngược..."
        ).build()

        startForeground(1001, initialNotification)

        // Sau đó bắt đầu cập nhật realtime
        notificationHelper.showNotificationRealtime(
            title = "Countdown Service",
            timeStart = System.currentTimeMillis(),
            numberCountDown = alarmRepository.timeAlarm,
            notificationId = 1001,
            scope = serviceScope
        )
    }
}