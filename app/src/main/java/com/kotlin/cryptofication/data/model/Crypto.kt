package com.kotlin.cryptofication.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Crypto(
    var id: String?,
    var name: String?,
    var symbol: String?,
    var image: String?,
    var current_price: Double,
    var market_cap_rank: Int,
    var high_24h: Double,
    var low_24h: Double,
    var price_change_24h: Double,
    var price_change_percentage_24h: Double,
    var sparkline_in_7d: CryptoSparkline? = null
) : Parcelable
