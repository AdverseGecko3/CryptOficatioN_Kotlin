package com.kotlin.cryptofication.data.model

import android.os.Parcel
import android.os.Parcelable

data class CryptoModel(var id: String, var name: String, var symbol: String, var image: String,
                       var current_price: Double, var market_cap_rank: Int, var high_24h: Double,
                       var low_24h: Double, var price_change_percentage_24h: Double) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(symbol)
        parcel.writeString(image)
        parcel.writeDouble(current_price)
        parcel.writeInt(market_cap_rank)
        parcel.writeDouble(high_24h)
        parcel.writeDouble(low_24h)
        parcel.writeDouble(price_change_percentage_24h)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CryptoModel> {
        override fun createFromParcel(parcel: Parcel): CryptoModel {
            return CryptoModel(parcel)
        }

        override fun newArray(size: Int): Array<CryptoModel?> {
            return arrayOfNulls(size)
        }
    }
}
