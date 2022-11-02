package com.adversegecko3.cryptofication.domain

import android.util.Log
import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.repos.CryptoRepository
import javax.inject.Inject

class GetCryptoDataMarketOnlineUseCase @Inject constructor(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(query: String): List<Crypto> {
        val response = repository.getCryptoDataMarket(query)
        Log.d("CryptoServiceM", "Response Repository: $response")
        return response
    }
}