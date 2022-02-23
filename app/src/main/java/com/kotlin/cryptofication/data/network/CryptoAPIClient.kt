package com.kotlin.cryptofication.data.network

import com.kotlin.cryptofication.data.model.Crypto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoAPIClient {
    /* Required params: vs_currency, order, per_page, page, sparkline
    Check the params at https://www.coingecko.com/es/api/documentation
    ?vs_currency=usd&order=market_cap_desc&per_page=250&page=1&sparkline=false */
    @GET("coins/markets")
    suspend fun getMarketCryptoList(
        @Query("vs_currency") currency: String, @Query("per_page") perPage: String,
        @Query("sparkline") sparkline: String, @Query("page") page: Int
    ): Response<List<Crypto>>

    /* Required params: ids, vs_currency, order, per_page, page, sparkline
    Check the params at https://www.coingecko.com/es/api/documentation
    ?ids=bitcoin,dogecoin&vs_currency=usd&order=market_cap_desc&per_page=250&page=1&sparkline=false */
    @GET("coins/markets")
    suspend fun getAlertsCryptoList(
        @Query("ids") ids: String, @Query("vs_currency") currency: String,
        @Query("sparkline") sparkline: String
    ): Response<List<Crypto>>
}