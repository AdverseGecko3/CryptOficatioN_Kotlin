package com.kotlin.cryptofication.data.repos

import android.util.Log
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.network.CryptoService

class CryptoRepository {
    private val api = CryptoService()

    suspend fun getAllCryptoMarket(page: Int): List<Crypto> {
        var response = api.getMarketCrypto(page)
        Log.d("CryptoService", "Response Repository: $response")
        if (!response.isNullOrEmpty())
            if (page == 1) {
                CryptoProvider.cryptosMarket = response
            } else {
                CryptoProvider.cryptosMarket = CryptoProvider.cryptosMarket + response
                response = CryptoProvider.cryptosMarket
            }
        return response
    }

    suspend fun getAllCryptoAlerts(): List<Crypto> {
        val response = api.getAlertCrypto()
        Log.d("CryptoService", "Response: $response")
        if (!response.isNullOrEmpty()) CryptoProvider.cryptosAlerts = response
        return response
    }
}