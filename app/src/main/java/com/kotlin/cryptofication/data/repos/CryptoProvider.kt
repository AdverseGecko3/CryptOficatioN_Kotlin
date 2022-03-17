package com.kotlin.cryptofication.data.repos

import com.kotlin.cryptofication.data.model.Crypto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoProvider @Inject constructor() {
    var cryptosMarket: List<Crypto> = emptyList()
    var cryptosAlerts: List<Crypto> = emptyList()
}