package com.adversegecko3.cryptofication.domain

import android.util.Log
import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.model.CryptoAlert
import com.adversegecko3.cryptofication.data.repos.CryptoAlertRepository
import com.adversegecko3.cryptofication.data.repos.CryptoProvider
import com.adversegecko3.cryptofication.data.repos.CryptoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCryptoAlertsOnlineUseCase @Inject constructor(
    private val repository: CryptoRepository,
    private val cryptoProvider: CryptoProvider,
    private val mRoom: CryptoAlertRepository
) {
    suspend operator fun invoke(): List<Crypto> {
        val response = repository.getAllCryptoAlerts()
        Log.d("CryptoServiceA", "Response: $response")
        if (response.isNotEmpty()) {
            // Save Bitcoin crypto
            response.forEach {
                if (it.symbol.uppercase() == "BTC") {
                    cryptoProvider.cryptoBitcoin = it
                }
            }

            val alertsHasBitcoin = checkIfAlertsHasBitcoin()
            cryptoProvider.cryptosAlerts = if (alertsHasBitcoin) {
                response
            } else {
                removeBitcoinFromResponse(response)
            }
        }
        return response
    }

    suspend fun checkIfAlertsHasBitcoin(): Boolean {
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
        return alertsHasBitcoin
    }

    private fun removeBitcoinFromResponse(response: List<Crypto>): List<Crypto> {
        val responseM = response.toMutableList()
        run loop@{
            response.forEach {
                if (it.symbol.uppercase() == "BTC") {
                    responseM.remove(it)
                    return@loop
                }
            }
        }
        return responseM.toList()
    }
}