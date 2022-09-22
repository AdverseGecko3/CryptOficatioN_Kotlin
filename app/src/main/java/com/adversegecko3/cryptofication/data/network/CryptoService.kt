package com.adversegecko3.cryptofication.data.network

import android.util.Log
import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.repos.CryptoAlertRepository
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

class CryptoService @Inject constructor(
    private val api: CryptoAPIClient,
    private val mRoom: CryptoAlertRepository
) {

    suspend fun getMarketCrypto(page: Int): List<Crypto> {
        return withContext(Dispatchers.IO) {
            try {
                val userCurrency = mPrefs.getCurrency()
                val userItemsPage = mPrefs.getItemsPerPage()
                val response = api.getMarketCryptoList(
                    userCurrency, userItemsPage, "true", page
                )
                response.body() ?: emptyList()
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                e.message?.let { Log.e("CryptoService", it) }
                emptyList()
            } catch (e: IOException) {
                e.printStackTrace()
                e.message?.let { Log.e("CryptoService", it) }
                emptyList()
            }
        }
    }

    suspend fun getAlertCrypto(): List<Crypto> {
        return withContext(Dispatchers.IO) {
            try {
                val userCurrency = mPrefs.getCurrency()
                val cryptoAlertsList = mRoom.getAllAlerts()
                val listAlert: List<Crypto> = if (cryptoAlertsList.isNotEmpty()) {
                    var ids = ""
                    for (crypto in cryptoAlertsList) {
                        if (crypto.symbol.uppercase() != "BTC") {
                            ids += "${crypto.id},"
                        }
                    }
                    ids += "bitcoin"
                    val response = api.getAlertsCryptoList(
                        ids, userCurrency, "true"
                    )
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
                listAlert
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                e.message?.let { Log.e("CryptoService", it) }
                emptyList()
            }
        }
    }
}