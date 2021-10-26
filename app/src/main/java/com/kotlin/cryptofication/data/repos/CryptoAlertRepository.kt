package com.kotlin.cryptofication.data.repos

import android.app.Application
import com.kotlin.cryptofication.data.model.CryptoAlert
import com.kotlin.cryptofication.data.model.CryptoAlertDB

class CryptoAlertRepository(application: Application) {
    private val database = CryptoAlertDB.getDatabase(application)

    private val dao = database.cryptoAlertDAO()

    suspend fun getAllAlerts(): List<CryptoAlert> = dao.getAll()

    suspend fun insertAlert(cryptoAlert: CryptoAlert) = dao.insert(cryptoAlert)

    suspend fun deleteAlert(cryptoAlert: CryptoAlert) = dao.delete(cryptoAlert)
}