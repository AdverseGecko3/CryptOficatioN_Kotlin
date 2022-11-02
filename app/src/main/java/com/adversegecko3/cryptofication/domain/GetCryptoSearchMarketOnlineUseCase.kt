package com.adversegecko3.cryptofication.domain

import android.util.Log
import com.adversegecko3.cryptofication.data.model.CryptoSearch
import com.adversegecko3.cryptofication.data.repos.CryptoProvider
import com.adversegecko3.cryptofication.data.repos.CryptoRepository
import javax.inject.Inject

class GetCryptoSearchMarketOnlineUseCase @Inject constructor(
    private val repository: CryptoRepository,
    private val cryptoProvider: CryptoProvider
) {
    suspend operator fun invoke(query: String): List<Any> {
        val response = repository.getAllCryptoSearchMarket(query)
        Log.d("CryptoServiceM", "Response Repository($query): $response")
        if (response.isNotEmpty())
            if (response[0] !is String)
                cryptoProvider.cryptosSearchMarket = response.map { it as CryptoSearch }
        return response
    }
}