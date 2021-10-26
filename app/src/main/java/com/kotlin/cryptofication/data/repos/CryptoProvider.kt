package com.kotlin.cryptofication.data.repos

import com.kotlin.cryptofication.data.model.Crypto

class CryptoProvider {
    companion object {
        var cryptosMarket: List<Crypto> = emptyList()
        var cryptosAlerts: List<Crypto> = emptyList()
    }
}