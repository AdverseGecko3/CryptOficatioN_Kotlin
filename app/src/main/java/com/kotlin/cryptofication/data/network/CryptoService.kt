package com.kotlin.cryptofication.data.network

import android.util.Log
import com.kotlin.cryptofication.core.RetrofitHelper
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class CryptoService {

    private val retrofit = RetrofitHelper.getRetrofit()

    suspend fun getMarketCrypto(): List<Crypto> {
        return withContext(Dispatchers.IO) {
            try {
                val userCurrency = mPrefs.getCurrency()
                val userItemsPage = mPrefs.getItemsPerPage()
                Log.d("CryptoService", "userCurrency: $userCurrency")
                val response = retrofit.create(CryptoAPIClient::class.java).getMarketCryptoList(
                    userCurrency, userItemsPage, "false"
                )
                Log.d("CryptoService", "Response: $response")
                response.body() ?: emptyList()
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                Log.d("CryptoService", e.message!!)
                emptyList()
            }
        }
    }

    suspend fun getAlertCrypto(): List<Crypto> {
        return withContext(Dispatchers.IO) {
            try {
                val userCurrency = mPrefs.getCurrency()
                val idsList = mRoom.getAllAlerts()
                var ids = ""
                for(cryptoId in idsList) {
                    ids += "${cryptoId.id},"
                    Log.d("CryptoService", "ids: $ids")
                }
                ids = ids.substring(0, ids.length - 1)
                Log.d("CryptoService", "ids: $ids")
                Log.d("CryptoService", "userCurrency: $userCurrency")
                val response = retrofit.create(CryptoAPIClient::class.java).getAlertsCryptoList(
                    ids, userCurrency, "false"
                )
                Log.d("CryptoService", "Response: $response")
                response.body() ?: emptyList()
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                Log.d("CryptoService", e.message!!)
                emptyList()
            }
        }
    }
}