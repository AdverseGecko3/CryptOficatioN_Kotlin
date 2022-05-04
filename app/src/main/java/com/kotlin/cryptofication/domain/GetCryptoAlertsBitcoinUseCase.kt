package com.kotlin.cryptofication.domain

import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.repos.CryptoProvider
import javax.inject.Inject

class GetCryptoAlertsBitcoinUseCase @Inject constructor(private val cryptoProvider: CryptoProvider) {
    operator fun invoke(): Crypto? = cryptoProvider.cryptoBitcoin
}