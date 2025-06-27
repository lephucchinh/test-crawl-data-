package com.apero.testcrawldata.alarmreceiver.repository

import android.content.Context

interface AlarmRepository {
    fun setAlarm(context: Context, requestTime: Long)
}