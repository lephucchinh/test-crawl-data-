package com.apero.testcrawldata.alarmreceiver.repository

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import com.apero.testcrawldata.alarmreceiver.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmRepositoryImpl : AlarmRepository {

    private val _timeAlarm = MutableStateFlow(0L)
    override val timeAlarm = _timeAlarm.asStateFlow()
    override var requestTime: Long = 0

    private var countdownJob: Job? = null

    @SuppressLint("ScheduleExactAlarm")
    override fun setAlarm(context: Context, requestTimeService: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        requestTime = requestTimeService
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + requestTimeService * 1000L
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    alarmClockInfo,
                    pendingIntent
                )
                startCountdown(requestTimeService)
            } else {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        } else {
            alarmManager.setAlarmClock(
                alarmClockInfo,
                pendingIntent
            )
            startCountdown(requestTimeService)
        }
    }

    private fun startCountdown(seconds: Long) {
        countdownJob?.cancel()
        countdownJob = CoroutineScope(Dispatchers.Default).launch {
            val endTime = SystemClock.elapsedRealtime() + seconds * 1000L
            while (true) {
                val remainingMillis = endTime - SystemClock.elapsedRealtime()
                val remainingSeconds = (remainingMillis / 1000L).coerceAtLeast(0L)
                _timeAlarm.emit(remainingSeconds)
                if (remainingSeconds == 0L) break
                delay(200L) // delay ngắn để update UI mượt hơn
            }
        }
    }
}