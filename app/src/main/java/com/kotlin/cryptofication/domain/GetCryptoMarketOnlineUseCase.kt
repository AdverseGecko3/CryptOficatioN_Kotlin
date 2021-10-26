package com.kotlin.cryptofication.domain

import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.repos.CryptoRepository

class GetCryptoMarketOnlineUseCase {

    private val repository = CryptoRepository()

    suspend operator fun invoke(): List<Crypto> = repository.getAllCryptoMarket()
}