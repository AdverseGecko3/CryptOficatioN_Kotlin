package com.kotlin.cryptofication.domain

import com.kotlin.cryptofication.data.CryptoRepository
import com.kotlin.cryptofication.data.model.CryptoModel

class GetCryptoOnlineUseCase {
    private val repository = CryptoRepository()

    suspend operator fun invoke(): List<CryptoModel> = repository.getAllCrypto()

}