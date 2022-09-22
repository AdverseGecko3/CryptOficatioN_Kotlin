package com.adversegecko3.cryptofication.domain

import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.repos.CryptoProvider
import javax.inject.Inject

class GetCryptoAlertsOfflineUseCase @Inject constructor(private val cryptoProvider: CryptoProvider) {
    operator fun invoke(): List<Crypto> = cryptoProvider.cryptosAlerts
}