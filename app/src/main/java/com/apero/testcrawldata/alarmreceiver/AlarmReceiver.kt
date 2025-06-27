package com.apero.testcrawldata.alarmreceiver

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.apero.testcrawldata.permissionadmin.MyDeviceAdminReceiver

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val dpm = it.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(it, MyDeviceAdminReceiver::class.java)

            if (dpm.isAdminActive(adminComponent)) {
                dpm.lockNow() // Tắt màn hình
            } else {
                Toast.makeText(it, "Chưa cấp quyền Device Admin", Toast.LENGTH_SHORT).show()
            }
        }
    }
}