package com.kotlin.cryptofication.data.repos

import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.network.CryptoService
import javax.inject.Inject

class CryptoRepository @Inject constructor(private val api: CryptoService) {
    suspend fun getAllCryptoMarket(page: Int): List<Crypto> = api.getMarketCrypto(page)

    suspend fun getAllCryptoAlerts(): List<Crypto> = api.getAlertCrypto()
}