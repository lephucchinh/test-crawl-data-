package com.apero.testcrawldata

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apero.testcrawldata.DI.DIContainer.alarmRepository
import com.apero.testcrawldata.alarmreceiver.repository.AlarmRepository
import com.apero.testcrawldata.alarmreceiver.repository.AlarmRepositoryImpl
import com.apero.testcrawldata.permissionadmin.MyDeviceAdminReceiver
import com.apero.testcrawldata.service.CountdownService
import com.apero.testcrawldata.ui.theme.TestCrawlDataTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermission()
        requestNotificationPermission()
        setContent {
            TestCrawlDataTheme {
                val focusManager = LocalFocusManager.current
                val timeState by alarmRepository.timeAlarm.collectAsStateWithLifecycle()
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            focusManager.clearFocus()
                        }
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val textState = remember { mutableStateOf("") }

                        MyEditTextFocusAware(textState = textState)

                        Button(
                            onClick = {
                                val value = textState.value.toLongOrNull() ?: 0L
                                if(value != 0L || timeState == 0L) {
                                    alarmRepository.setAlarm(this@MainActivity, value)
                                    val intent = Intent(this@MainActivity, CountdownService::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        startForegroundService(intent)
                                    }
                                }
                            }
                        ) {
                            Text("Set Alarm")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(timeState.toString())
                    }
                }
            }
        }
    }


    private fun requestPermission() {
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Cho phép quyền để app khóa màn hình"
            )
        }
        startActivity(intent)
    }

    @Composable
    fun MyEditTextFocusAware(
        textState: MutableState<String>
    ) {
        TextField(
            value = textState.value,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    textState.value = newValue
                }
            },
            label = { Text("Nhập số") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

    }
    private val NOTIFICATION_PERMISSION_CODE = 1001

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            } /*else {
                // Quyền đã được cấp
                showNotification()
            }*/
        } /*else {
            // Không cần xin quyền trên Android dưới 13
            showNotification()
        }*/
    }
}

