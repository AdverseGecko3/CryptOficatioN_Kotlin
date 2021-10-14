package com.kotlin.cryptofication.data.network

import com.kotlin.cryptofication.data.model.CryptoModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoAPIClient {
    /* Required params: vs_currency, order, per_page, page, sparkline
    Check the params at https://www.coingecko.com/es/api/documentation
    ?vs_currency=usd&order=market_cap_desc&per_page=250&page=1&sparkline=false */
    @GET("coins/markets")
    suspend fun getCryptoList(
        @Query("vs_currency") currency: String?, @Query("order") order: String?,
        @Query("per_page") perPage: String?, @Query("page") page: String?,
        @Query("sparkline") sparkline: String?
    ): Response<List<CryptoModel>>
}