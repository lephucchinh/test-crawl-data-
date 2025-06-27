package com.apero.testcrawldata.service.repository

import android.os.CountDownTimer

class CountdownRepositoryImpl : CountdownRepository {

    private var countDownTimer: CountDownTimer? = null
    private var remainingTime: Long = 0L
    private var callback: CountdownRepository.CountdownCallback? = null

    override fun setCallback(callback: CountdownRepository.CountdownCallback) {
        this.callback = callback
    }

    override fun startCountdown(durationMillis: Long, interval: Long) {
        countDownTimer?.cancel()
        remainingTime = durationMillis

        countDownTimer = object : CountDownTimer(durationMillis, interval) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                callback?.onTick(millisUntilFinished)
            }

            override fun onFinish() {
                remainingTime = 0L
                callback?.onFinish()
            }
        }.start()
    }

    override fun stopCountdown() {
        countDownTimer?.cancel()
        remainingTime = 0L
    }

    override fun getRemainingTime(): Long = remainingTime
}