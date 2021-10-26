package com.kotlin.cryptofication.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_crypto")
data class CryptoAlert(
    @PrimaryKey
    var id: String,
    @ColumnInfo (name = "pos_added")
    var posAdded: Int = 0
)
