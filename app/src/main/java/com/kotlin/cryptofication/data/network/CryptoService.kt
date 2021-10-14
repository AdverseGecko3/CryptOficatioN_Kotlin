package com.kotlin.cryptofication.data.network

import android.util.Log
import com.kotlin.cryptofication.core.RetrofitHelper
import com.kotlin.cryptofication.classes.CryptOficatioNApp
import com.kotlin.cryptofication.classes.Preferences
import com.kotlin.cryptofication.data.model.CryptoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CryptoService {

    private val retrofit = RetrofitHelper.getRetrofit()

    suspend fun getCrypto(): List<CryptoModel> {
        return withContext(Dispatchers.IO) {
            val preferences = Preferences(CryptOficatioNApp.appContext())
            val userCurrency: String = preferences.getCurrency()
            val userItemsPage: String = preferences.getItemsPerPage()
            Log.d("CryptoService", "userCurrency: $userCurrency")
            val response = retrofit.create(CryptoAPIClient::class.java).getCryptoList(
                userCurrency, "market_cap_desc", userItemsPage, "1", "false"
            )
            Log.d("CryptoService", "Response: $response")
            response.body() ?: emptyList()
        }
    }
}