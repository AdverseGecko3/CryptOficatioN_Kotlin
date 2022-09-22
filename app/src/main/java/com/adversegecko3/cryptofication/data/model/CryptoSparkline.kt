package com.adversegecko3.cryptofication.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CryptoSparkline(
    val price: List<Double>
) : Parcelable