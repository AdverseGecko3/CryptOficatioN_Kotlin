package com.kotlin.cryptofication.data.repos

import android.util.Log
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.network.CryptoService

class CryptoRepository {
    private val api = CryptoService()

    suspend fun getAllCryptoMarket(): List<Crypto> {
        val response = api.getMarketCrypto()
        Log.d("CryptoService", "Response: $response")
        if (!response.isNullOrEmpty()) CryptoProvider.cryptosMarket = response
        return response
    }

    suspend fun getAllCryptoAlerts(): List<Crypto> {
        val response = api.getAlertCrypto()
        Log.d("CryptoService", "Response: $response")
        if (!response.isNullOrEmpty()) CryptoProvider.cryptosAlerts = response
        return response
    }
}