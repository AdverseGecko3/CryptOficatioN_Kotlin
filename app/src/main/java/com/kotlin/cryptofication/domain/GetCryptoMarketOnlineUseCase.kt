package com.kotlin.cryptofication.domain

import android.util.Log
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.repos.CryptoProvider
import com.kotlin.cryptofication.data.repos.CryptoRepository
import javax.inject.Inject

class GetCryptoMarketOnlineUseCase @Inject constructor(
    private val repository: CryptoRepository,
    private val cryptoProvider: CryptoProvider
) {
    suspend operator fun invoke(page: Int = 1): List<Crypto> {
        var response = repository.getAllCryptoMarket(page)
        Log.d("CryptoService", "Response Repository: $response")
        if (!response.isNullOrEmpty())
            if (page == 1) {
                cryptoProvider.cryptosMarket = response
            } else {
                cryptoProvider.cryptosMarket = cryptoProvider.cryptosMarket + response
                response = cryptoProvider.cryptosMarket
            }
        return response
    }
}