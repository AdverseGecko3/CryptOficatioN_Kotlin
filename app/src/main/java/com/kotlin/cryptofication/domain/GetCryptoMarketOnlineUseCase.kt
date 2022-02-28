package com.kotlin.cryptofication.domain

import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.repos.CryptoRepository
import javax.inject.Inject

class GetCryptoMarketOnlineUseCase @Inject constructor(private val repository: CryptoRepository) {
    suspend operator fun invoke(page: Int = 1): List<Crypto> = repository.getAllCryptoMarket(page)
}