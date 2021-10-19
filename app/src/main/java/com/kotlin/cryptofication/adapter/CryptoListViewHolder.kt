package com.kotlin.cryptofication.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.prefs
import com.kotlin.cryptofication.databinding.AdapterCryptoListBinding
import com.kotlin.cryptofication.data.model.CryptoModel
import com.kotlin.cryptofication.utilities.customFormattedPercentage
import com.kotlin.cryptofication.utilities.customFormattedPrice
import com.kotlin.cryptofication.utilities.negativePrice
import com.kotlin.cryptofication.utilities.positivePrice
import com.squareup.picasso.Picasso

class CryptoListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = AdapterCryptoListBinding.bind(itemView)

    fun bind(crypto: CryptoModel) {
        Picasso.get().load(crypto.image).into(binding.ivAdapterCryptoIcon)
        binding.tvAdapterCryptoSymbol.text = crypto.symbol.uppercase()
        binding.tvAdapterCryptoName.text = crypto.name
        val userCurrency = when (prefs.getCurrency()) {
            "eur" -> CryptOficatioNApp.appContext.getString(R.string.CURRENCY_EURO)
            "usd" -> CryptOficatioNApp.appContext.getString(R.string.CURRENCY_DOLLAR)
            else -> CryptOficatioNApp.appContext.getString(R.string.CURRENCY_DOLLAR)
        }
        val currentPrice = crypto.current_price.customFormattedPrice(userCurrency)
        binding.tvAdapterCryptoPrice.text = currentPrice
        val priceChange = crypto.price_change_percentage_24h.customFormattedPercentage()
        binding.tvAdapterCryptoTextPriceChange.text = priceChange
        if (crypto.price_change_percentage_24h >= 0) {
            binding.ivAdapterCryptoIconPriceChange.positivePrice()
            binding.tvAdapterCryptoTextPriceChange.positivePrice()
        } else {
            binding.ivAdapterCryptoIconPriceChange.negativePrice()
            binding.tvAdapterCryptoTextPriceChange.negativePrice()
        }
    }
}