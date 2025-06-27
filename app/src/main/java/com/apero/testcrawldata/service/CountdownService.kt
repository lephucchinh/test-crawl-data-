package com.apero.testcrawldata.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.apero.testcrawldata.DIContainer
import com.apero.testcrawldata.service.repository.CountdownRepository

class CountdownService : Service() {
    private val binder = LocalBinder()
    private val countdownRepository = DIContainer.countdownRepository

    var callback: CountdownCallback? = null

    interface CountdownCallback {
        fun onTick(millisUntilFinished: Long)
        fun onFinish()
    }


    inner class LocalBinder : Binder() {
        fun getService(): CountdownService = this@CountdownService
    }

    fun startCountdown(durationMillis: Long) {
        countdownRepository.setCallback(object : CountdownRepository.CountdownCallback {
            override fun onTick(millisUntilFinished: Long) {
                callback?.onTick(millisUntilFinished)
            }

            override fun onFinish() {
                callback?.onFinish()
            }
        })
        countdownRepository.startCountdown(durationMillis)
    }

    fun stopCountdown() {
        countdownRepository.stopCountdown()
    }

    fun getRemainingTime(): Long {
        return countdownRepository.getRemainingTime()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownRepository.stopCountdown()
    }


    override fun onBind(p0: Intent?): IBinder? = binder
}