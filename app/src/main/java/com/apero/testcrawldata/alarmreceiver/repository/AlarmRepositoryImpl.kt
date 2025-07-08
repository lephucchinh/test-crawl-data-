package com.apero.testcrawldata.alarmreceiver.repository

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.apero.testcrawldata.alarmreceiver.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmRepositoryImpl : AlarmRepository {

    private val _timeAlarm = MutableStateFlow<Long>(0L) // đơn vị: giây còn lại
    override val timeAlarm = _timeAlarm.asStateFlow()

    private var countdownJob: Job? = null

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun setAlarm(context: Context, requestTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + requestTime * 1000L

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

        // Bắt đầu đếm ngược
        startCountdown(requestTime)
    }

    private fun startCountdown(seconds: Long) {
        countdownJob?.cancel()
        countdownJob = CoroutineScope(Dispatchers.Default).launch {
            var remaining = seconds
            while (remaining >= 0) {
                _timeAlarm.emit(remaining)
                delay(1000L)
                remaining--
            }
        }
    }
}