package com.adversegecko3.cryptofication.data.network

import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.model.CryptoSearchCoins
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoAPIClient {
    /* Required params: vs_currency, order, per_page, page, sparkline
    Check the params at https://www.coingecko.com/es/api/documentation
    ?vs_currency=usd&order=market_cap_desc&per_page=250&page=1&sparkline=false */
    @GET("coins/markets")
    suspend fun getMarketCryptoList(
        @Query("vs_currency") currency: String,
        @Query("per_page") perPage: String,
        @Query("sparkline") sparkline: String,
        @Query("page") page: Int
    ): Response<List<Crypto>>

    /* Required params: string
    Check the params at https://www.coingecko.com/es/api/documentation
    ?string=bit */
    @GET("search")
    suspend fun getMarketSearchCryptoList(
        @Query("query") string: String
    ): Response<CryptoSearchCoins>

    /* Required params: ids, vs_currency, order, per_page, page, sparkline
    Check the params at https://www.coingecko.com/es/api/documentation
    ?ids=bitcoin,dogecoin&vs_currency=usd&order=market_cap_desc&per_page=250&page=1&sparkline=false */
    @GET("coins/markets")
    suspend fun getCryptoListByIds(
        @Query("ids") ids: String,
        @Query("vs_currency") currency: String,
        @Query("sparkline") sparkline: String
    ): Response<List<Crypto>>
}