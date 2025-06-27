package com.apero.testcrawldata.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class CountdownServiceConnector(
    private val context: Context,
    private val onTickCallBack: (Long) -> Unit,
    private val onFinishCallBack: () -> Unit
) {
    private var countdownService: CountdownService? = null
    private var isBound = false
    private var pendingCountdownMillis: Long? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CountdownService.LocalBinder
            countdownService = binder.getService()
            isBound = true

            countdownService?.callback = object : CountdownService.CountdownCallback {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d("dkm", "onTick: $millisUntilFinished")
                    onTickCallBack(millisUntilFinished)
                }

                override fun onFinish() = onFinishCallBack()
            }

            pendingCountdownMillis?.let {
                countdownService?.startCountdown(it)
                pendingCountdownMillis = null
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            countdownService = null
        }
    }

    fun bind() {
        val intent = Intent(context, CountdownService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }

    fun startCountdown(timeMillis: Long) {
        if (isBound) {
            countdownService?.startCountdown(timeMillis)
        } else {
            pendingCountdownMillis = timeMillis
        }
    }
}