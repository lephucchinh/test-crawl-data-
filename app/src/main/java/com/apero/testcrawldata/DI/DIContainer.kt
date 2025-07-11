package com.apero.testcrawldata.DI

import com.apero.testcrawldata.alarmreceiver.repository.AlarmRepository
import com.apero.testcrawldata.alarmreceiver.repository.AlarmRepositoryImpl
import com.apero.testcrawldata.service.NotificationHelper

object DIContainer {
    val alarmRepository: AlarmRepository = AlarmRepositoryImpl()

}