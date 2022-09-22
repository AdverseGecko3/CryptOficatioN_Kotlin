package com.adversegecko3.cryptofication.data.repos

import com.adversegecko3.cryptofication.data.model.Crypto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoProvider @Inject constructor() {
    var cryptosMarket: List<Crypto> = emptyList()
    var cryptosAlerts: List<Crypto> = emptyList()
    var cryptoBitcoin: Crypto? = null
}