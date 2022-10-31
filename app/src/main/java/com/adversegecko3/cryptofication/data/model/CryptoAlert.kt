package com.adversegecko3.cryptofication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_crypto")
data class CryptoAlert(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "symbol")
    val symbol: String,

    @ColumnInfo(name = "current_price")
    var current_price: Double = 0.0,

    @ColumnInfo(name = "quantity")
    var quantity: Double = 0.0
)