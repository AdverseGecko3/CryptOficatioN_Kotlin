package com.kotlin.cryptofication.domain

import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.repos.CryptoProvider

class GetCryptoAlertsOfflineUseCase {
    operator fun invoke(): List<Crypto> = CryptoProvider.cryptosAlerts
}