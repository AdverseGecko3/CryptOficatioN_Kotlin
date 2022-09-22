package com.adversegecko3.cryptofication.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Crypto(
    val id: String,
    val name: String,
    val symbol: String,
    val image: String,
    val current_price: Double,
    val market_cap_rank: Int,
    val high_24h: Double,
    val low_24h: Double,
    val price_change_24h: Double,
    val price_change_percentage_24h: Double,
    val sparkline_in_7d: CryptoSparkline? = null
) : Parcelable