package com.kotlin.cryptofication.data

import android.util.Log
import com.kotlin.cryptofication.data.model.CryptoModel
import com.kotlin.cryptofication.data.model.CryptoProvider
import com.kotlin.cryptofication.data.network.CryptoService

class CryptoRepository {

    private val api = CryptoService()

    suspend fun getAllCrypto(): List<CryptoModel> {
        val response = api.getCrypto()
        Log.d("CryptoService", "Response: $response")
        CryptoProvider.cryptos = response
        return response
    }
}