package com.apero.testcrawldata

import com.apero.testcrawldata.service.CountdownService
import com.apero.testcrawldata.service.repository.CountdownRepository
import com.apero.testcrawldata.service.repository.CountdownRepositoryImpl

object DIContainer {
    val countdownRepository: CountdownRepository by lazy {
        CountdownRepositoryImpl()
    }
}