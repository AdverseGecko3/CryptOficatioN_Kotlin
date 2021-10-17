package com.kotlin.cryptofication.ui.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.data.model.CryptoModel
import com.kotlin.cryptofication.databinding.CryptoDetailDialogBinding
import java.text.DecimalFormat

class DialogCryptoDetail(
    private val selectedCrypto: CryptoModel,
    private val userCurrency: String
) : DialogFragment() {
    private var _binding: CryptoDetailDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        _binding = CryptoDetailDialogBinding.inflate(layoutInflater)

        val currencySeparator = DecimalFormat().decimalFormatSymbols.decimalSeparator

        builder.setView(binding.root)
        binding.tvDialogCryptoDetailName.text = selectedCrypto.name
        binding.tvDialogCryptoDetailSymbolText.text = selectedCrypto.symbol.uppercase()
        binding.tvDialogCryptoDetailMarketCapRankText.text =
            selectedCrypto.market_cap_rank.toString()
        binding.tvDialogCryptoDetailPriceChangePercentage24hText.text = (String
            .format("%.2f", selectedCrypto.price_change_percentage_24h)
            .replace("0+$".toRegex(), "") + "%")
        if (selectedCrypto.price_change_percentage_24h >= 0) {
            binding.tvDialogCryptoDetailPriceChangePercentage24hText.setTextColor(
                ContextCompat
                    .getColor(requireContext(), R.color.green_high)
            )
        } else {
            binding.tvDialogCryptoDetailPriceChangePercentage24hText.setTextColor(
                ContextCompat
                    .getColor(requireContext(), R.color.red_low)
            )
        }
        var high24h = String.format("%.10f", selectedCrypto.high_24h)
            .replace("0+$".toRegex(), "")
        if (high24h.endsWith(currencySeparator)) {
            high24h = high24h.substring(0, high24h.length - 1)
        }
        binding.tvDialogCryptoDetailHigh24hText.text = "$high24h $userCurrency"
        var low24h = String.format("%.10f", selectedCrypto.low_24h)
            .replace("0+$".toRegex(), "")
        if (low24h.endsWith(currencySeparator)) {
            low24h = low24h.substring(0, low24h.length - 1)
        }
        binding.tvDialogCryptoDetailLow24hText.text = "$low24h $userCurrency"
        var currentPrice = String.format("%.10f", selectedCrypto.current_price)
            .replace("0+$".toRegex(), "")
        if (currentPrice.endsWith(currencySeparator)) {
            currentPrice = currentPrice.substring(0, currentPrice.length - 1)
        }
        binding.tvDialogCryptoDetailCurrentPriceText.text = "$currentPrice $userCurrency"
        builder.setNegativeButton(
            requireContext().getString(R.string.CLOSE)
        ) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
        val dialog = builder.show()

        // Change the negative button color and weight
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(
            ResourcesCompat.getColor(
                resources,
                R.color.purple_app_accent,
                null
            )
        )
        val layoutParams = negativeButton.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        negativeButton.layoutParams = layoutParams
        negativeButton.textSize = 17F

        return dialog
    }


}