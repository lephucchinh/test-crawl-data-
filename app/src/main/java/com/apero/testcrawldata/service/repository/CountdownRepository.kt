package com.apero.testcrawldata.service.repository

interface CountdownRepository {
    fun startCountdown(durationMillis: Long, interval: Long = 1000L)
    fun stopCountdown()
    fun getRemainingTime(): Long
    fun setCallback(callback: CountdownCallback)

    interface CountdownCallback {
        fun onTick(millisUntilFinished: Long)
        fun onFinish()
    }
}