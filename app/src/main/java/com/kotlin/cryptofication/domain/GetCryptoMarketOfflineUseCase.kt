package com.kotlin.cryptofication.domain

import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.repos.CryptoProvider

class GetCryptoMarketOfflineUseCase {
    operator fun invoke(): List<Crypto> = CryptoProvider.cryptosMarket
}