package com.adversegecko3.cryptofication.data.repos

import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.network.CryptoService
import javax.inject.Inject

class CryptoRepository @Inject constructor(private val api: CryptoService) {
    suspend fun getAllCryptoMarket(page: Int): List<Crypto> = api.getMarketCrypto(page)
    suspend fun getAllCryptoSearchMarket(query: String): List<Any> =
        api.getSearchMarketCrypto(query)
    suspend fun getCryptoDataMarket(query: String): List<Crypto> =
        api.getCryptoData(query)
    suspend fun getAllCryptoAlerts(): List<Crypto> = api.getAlertCrypto()
}