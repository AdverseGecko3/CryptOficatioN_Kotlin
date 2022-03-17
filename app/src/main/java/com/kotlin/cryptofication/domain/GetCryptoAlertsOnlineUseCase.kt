package com.kotlin.cryptofication.domain

import android.util.Log
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.repos.CryptoProvider
import com.kotlin.cryptofication.data.repos.CryptoRepository
import javax.inject.Inject

class GetCryptoAlertsOnlineUseCase @Inject constructor(
    private val repository: CryptoRepository,
    private val cryptoProvider: CryptoProvider
) {
    suspend operator fun invoke(): List<Crypto> {
        val response = repository.getAllCryptoAlerts()
        Log.d("CryptoService", "Response: $response")
        if (!response.isNullOrEmpty()) cryptoProvider.cryptosAlerts = response
        return response
    }
}