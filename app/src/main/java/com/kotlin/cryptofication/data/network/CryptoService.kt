package com.kotlin.cryptofication.data.network

import android.util.Log
import com.kotlin.cryptofication.core.RetrofitHelper
import com.kotlin.cryptofication.classes.CryptOficatioNApp.Companion.prefs
import com.kotlin.cryptofication.data.model.CryptoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class CryptoService {

    private val retrofit = RetrofitHelper.getRetrofit()

    suspend fun getCrypto(): List<CryptoModel> {
        return withContext(Dispatchers.IO) {
            try {
                val userCurrency = prefs.getCurrency()
                val userItemsPage = prefs.getItemsPerPage()
                Log.d("CryptoService", "userCurrency: $userCurrency")
                val response = retrofit.create(CryptoAPIClient::class.java).getCryptoList(
                    userCurrency, "market_cap_desc", userItemsPage, "1", "false"
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