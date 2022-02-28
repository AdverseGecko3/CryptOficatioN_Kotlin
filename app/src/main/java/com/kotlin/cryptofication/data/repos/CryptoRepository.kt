package com.kotlin.cryptofication.data.repos

import android.util.Log
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.network.CryptoService
import javax.inject.Inject

class CryptoRepository @Inject constructor(
    private val api: CryptoService,
    private val cryptoProvider: CryptoProvider
) {
    suspend fun getAllCryptoMarket(page: Int): List<Crypto> {
        var response = api.getMarketCrypto(page)
        Log.d("CryptoService", "Response Repository: $response")
        if (!response.isNullOrEmpty())
            if (page == 1) {
                cryptoProvider.cryptosMarket = response
            } else {
                cryptoProvider.cryptosMarket = cryptoProvider.cryptosMarket + response
                response = cryptoProvider.cryptosMarket
            }
        return response
    }

    suspend fun getAllCryptoAlerts(): List<Crypto> {
        val response = api.getAlertCrypto()
        Log.d("CryptoService", "Response: $response")
        if (!response.isNullOrEmpty()) cryptoProvider.cryptosAlerts = response
        return response
    }
}