package com.apero.testcrawldata.alarmreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apero.testcrawldata.DI.DIContainer
import com.apero.testcrawldata.service.CountdownService

class CancelAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            Log.d("CancelAlarmReceiver", "Cancel alarm received")
            
            // Cancel alarm through repository
            DIContainer.alarmRepository.cancelAlarm(ctx)
            
            // Stop countdown service
            val serviceIntent = Intent(ctx, CountdownService::class.java)
            ctx.stopService(serviceIntent)
            
            // Send broadcast to update UI if needed
            val updateIntent = Intent("com.apero.testcrawldata.ALARM_CANCELLED")
            ctx.sendBroadcast(updateIntent)
        }
    }
    
    companion object {
        const val ACTION_CANCEL_ALARM = "com.apero.testcrawldata.CANCEL_ALARM"
    }
} 