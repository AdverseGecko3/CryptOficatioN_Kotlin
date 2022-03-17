package com.kotlin.cryptofication.data.repos

import com.kotlin.cryptofication.data.model.CryptoAlert
import com.kotlin.cryptofication.data.model.CryptoAlertDAO
import javax.inject.Inject

class CryptoAlertRepository @Inject constructor(private val dao: CryptoAlertDAO) {

    suspend fun getAllAlerts(): List<CryptoAlert> = dao.getAll()

    suspend fun getSingleAlert(id: String): CryptoAlert? = dao.getSingleAlert(id)

    suspend fun insertAlert(cryptoAlert: CryptoAlert) = dao.insert(cryptoAlert)

    suspend fun modifyQuantityAlert(cryptoAlert: CryptoAlert) = dao.update(cryptoAlert)

    suspend fun deleteAlert(cryptoAlert: CryptoAlert) = dao.delete(cryptoAlert)
}