package com.adversegecko3.cryptofication.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CryptoSearchCoins(
    val coins: List<CryptoSearch>
)

@Parcelize
data class CryptoSearch(
    val id: String,
    val name: String,
    val symbol: String,
    val thumb: String,
    val market_cap_rank: Int
) : Parcelable