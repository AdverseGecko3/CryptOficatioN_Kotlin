package com.kotlin.cryptofication.domain

import com.kotlin.cryptofication.data.model.CryptoModel
import com.kotlin.cryptofication.data.model.CryptoProvider

class GetCryptoOfflineUseCase {
    operator fun invoke(): List<CryptoModel> = CryptoProvider.cryptos
}