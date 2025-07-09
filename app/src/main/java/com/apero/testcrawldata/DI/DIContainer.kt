package com.apero.testcrawldata.DI

import com.apero.testcrawldata.alarmreceiver.repository.AlarmRepository
import com.apero.testcrawldata.alarmreceiver.repository.AlarmRepositoryImpl

object DIContainer {
    val alarmRepository: AlarmRepository = AlarmRepositoryImpl()
}