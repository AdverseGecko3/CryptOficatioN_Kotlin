package com.kotlin.cryptofication.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.classes.CryptOficatioNApp
import com.kotlin.cryptofication.classes.CryptOficatioNApp.Companion.prefs
import com.kotlin.cryptofication.databinding.AdapterCryptoListBinding
import com.kotlin.cryptofication.data.model.CryptoModel
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.NumberFormat

class CryptoListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = AdapterCryptoListBinding.bind(itemView)

    @SuppressLint("SetTextI18n")
    fun bind(crypto: CryptoModel) {
        val currencySeparator = DecimalFormat().decimalFormatSymbols.decimalSeparator
        val nf = NumberFormat.getInstance()

        Picasso.get().load(crypto.image).into(binding.ivAdapterCryptoIcon)
        binding.tvAdapterCryptoSymbol.text = crypto.symbol.uppercase()
        binding.tvAdapterCryptoName.text = crypto.name
        var currentPrice = String.format("%.10f", crypto.current_price)
            .replace("0+$".toRegex(), "")
        if (currentPrice.endsWith(currencySeparator)) {
            currentPrice = currentPrice.substring(0, currentPrice.length - 1)
        }
        crypto.current_price = nf.parse(currentPrice)!!.toDouble()
        val userCurrency = when (prefs.getCurrency()) {
            "eur" -> CryptOficatioNApp.appContext.getString(R.string.CURRENCY_EURO)
            "usd" -> CryptOficatioNApp.appContext.getString(R.string.CURRENCY_DOLLAR)
            else -> CryptOficatioNApp.appContext.getString(R.string.CURRENCY_DOLLAR)
        }
        binding.tvAdapterCryptoPrice.text = "$currentPrice$userCurrency"
        var priceChange = String.format("%.2f", crypto.price_change_percentage_24h)
            .replace("0+$".toRegex(), "")
        if (priceChange.endsWith(currencySeparator)) {
            priceChange = priceChange.substring(0, priceChange.length - 1)
        }
        crypto.price_change_percentage_24h = nf.parse(priceChange)!!.toDouble()
        binding.tvAdapterCryptoTextPriceChange.text = "$priceChange%"
        if (crypto.price_change_percentage_24h >= 0) {
            binding.ivAdapterCryptoIconPriceChange.setImageResource(R.drawable.ic_arrow_drop_up)
            binding.ivAdapterCryptoIconPriceChange.setColorFilter(
                ContextCompat.getColor(CryptOficatioNApp.appContext, R.color.green_high)
            )
            binding.tvAdapterCryptoTextPriceChange.setTextColor(
                ContextCompat.getColor(CryptOficatioNApp.appContext, R.color.green_high)
            )
        } else {
            binding.ivAdapterCryptoIconPriceChange.setImageResource(R.drawable.ic_arrow_drop_down)
            binding.ivAdapterCryptoIconPriceChange.setColorFilter(
                ContextCompat.getColor(CryptOficatioNApp.appContext, R.color.red_low)
            )
            binding.tvAdapterCryptoTextPriceChange.setTextColor(
                ContextCompat.getColor(CryptOficatioNApp.appContext, R.color.red_low)
            )
        }
    }
}