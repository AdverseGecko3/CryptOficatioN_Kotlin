package com.kotlin.cryptofication.domain

import android.util.Log
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.model.CryptoAlert
import com.kotlin.cryptofication.data.repos.CryptoAlertRepository
import com.kotlin.cryptofication.data.repos.CryptoProvider
import com.kotlin.cryptofication.data.repos.CryptoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCryptoAlertsOnlineUseCase @Inject constructor(
    private val repository: CryptoRepository,
    private val cryptoProvider: CryptoProvider,
    private val mRoom: CryptoAlertRepository
) {
    suspend operator fun invoke(): List<Crypto> {
        var response = repository.getAllCryptoAlerts()
        Log.d("CryptoServiceA", "Response: $response")
        if (!response.isNullOrEmpty()) {
            // Save Bitcoin crypto
            response.forEach {
                if (it.symbol.uppercase() == "BTC") {
                    cryptoProvider.cryptoBitcoin = it
                }
            }

            // Check if alerts has bitcoin
            val cryptoListAlerts: List<CryptoAlert> =
                withContext(Dispatchers.IO) { mRoom.getAllAlerts() }
            var alertsHasBitcoin = false
            run loop@{
                cryptoListAlerts.forEach {
                    if (it.symbol.uppercase() == "BTC") {
                        alertsHasBitcoin = true
                        return@loop
                    }
                }
            }
            if (alertsHasBitcoin) {
                cryptoProvider.cryptosAlerts = response
            } else {
                val responseM = response.toMutableList()
                run loop@{
                    response.forEach {
                        if (it.symbol.uppercase() == "BTC") {
                            responseM.remove(it)
                            response = responseM.toList()
                            return@loop
                        }
                    }
                }
                cryptoProvider.cryptosAlerts = response
            }
        }
        return response
    }
}