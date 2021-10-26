package com.kotlin.cryptofication.ui.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.databinding.CryptoDetailDialogBinding
import com.kotlin.cryptofication.utilities.customFormattedPercentage
import com.kotlin.cryptofication.utilities.customFormattedPrice
import com.kotlin.cryptofication.utilities.negativePrice
import com.kotlin.cryptofication.utilities.positivePrice

class DialogCryptoDetail(
    private val selectedCrypto: Crypto,
    private val userCurrency: String
) : DialogFragment() {
    private var _binding: CryptoDetailDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        _binding = CryptoDetailDialogBinding.inflate(layoutInflater)
        builder.setView(binding.root)

        // Set the data to TextViews
        binding.tvDialogCryptoDetailName.text = selectedCrypto.name
        binding.tvDialogCryptoDetailSymbolText.text = selectedCrypto.symbol.uppercase()
        binding.tvDialogCryptoDetailMarketCapRankText.text =
            selectedCrypto.market_cap_rank.toString()
        val priceChange = selectedCrypto.price_change_percentage_24h.customFormattedPercentage()
        binding.tvDialogCryptoDetailPriceChangePercentage24hText.text = priceChange
        if (selectedCrypto.price_change_percentage_24h >= 0) {
            binding.tvDialogCryptoDetailPriceChangePercentage24hText.positivePrice()
        } else {
            binding.tvDialogCryptoDetailPriceChangePercentage24hText.negativePrice()
        }
        val high24h = selectedCrypto.high_24h.customFormattedPrice(userCurrency)
        binding.tvDialogCryptoDetailHigh24hText.text = high24h
        val low24h = selectedCrypto.low_24h.customFormattedPrice(userCurrency)
        binding.tvDialogCryptoDetailLow24hText.text = low24h
        val currentPrice = selectedCrypto.current_price.customFormattedPrice(userCurrency)
        binding.tvDialogCryptoDetailCurrentPriceText.text = currentPrice

        // Add a negative button
        builder.setNegativeButton(
            requireContext().getString(R.string.CLOSE)
        ) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
        val dialog = builder.show()

        // Change the negative button color, text size and weight
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(
            ResourcesCompat.getColor(
                resources,
                R.color.purple_app_accent,
                null
            )
        )
        negativeButton.textSize = 15F
        negativeButton.setPadding(2, 2, 2, 2)
        val layoutParams = negativeButton.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        negativeButton.layoutParams = layoutParams

        return dialog
    }


}