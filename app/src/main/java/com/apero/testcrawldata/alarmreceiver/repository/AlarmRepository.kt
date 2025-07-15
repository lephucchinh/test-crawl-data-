package com.apero.testcrawldata.alarmreceiver.repository

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface AlarmRepository {
    val timeAlarm : StateFlow<Long>
    val isAlarmActive: StateFlow<Boolean>
    var requestTime: Long
    fun setAlarm(context: Context, requestTime: Long)
    fun cancelAlarm(context: Context)
}