package com.adversegecko3.cryptofication.domain

import android.util.Log
import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.repos.CryptoProvider
import com.adversegecko3.cryptofication.data.repos.CryptoRepository
import javax.inject.Inject

class GetCryptoMarketOnlineUseCase @Inject constructor(
    private val repository: CryptoRepository,
    private val cryptoProvider: CryptoProvider
) {
    suspend operator fun invoke(page: Int = 1): List<Crypto> {
        var response = repository.getAllCryptoMarket(page)
        Log.d("CryptoServiceM", "Response Repository: $response")
        if (response.isNotEmpty())
            if (page == 1) {
                cryptoProvider.cryptosMarket = response
            } else {
                cryptoProvider.cryptosMarket = cryptoProvider.cryptosMarket + response
                response = cryptoProvider.cryptosMarket
            }
        return response
    }
}