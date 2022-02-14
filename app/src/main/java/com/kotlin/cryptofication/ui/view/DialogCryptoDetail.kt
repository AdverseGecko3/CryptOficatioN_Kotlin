package com.kotlin.cryptofication.ui.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.databinding.DialogCryptoDetailBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mResources
import com.kotlin.cryptofication.utilities.customFormattedPercentage
import com.kotlin.cryptofication.utilities.customFormattedPrice
import com.kotlin.cryptofication.utilities.negativePrice
import com.kotlin.cryptofication.utilities.positivePrice

class DialogCryptoDetail : BottomSheetDialogFragment(), OnChartValueSelectedListener {
    private var _binding: DialogCryptoDetailBinding? = null
    private val binding get() = _binding!!
    private var selectedCrypto: Crypto? = null
    private val userCurrency = mPrefs.getCurrencySymbol()

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCryptoDetailBinding.inflate(layoutInflater, container, false)

        selectedCrypto = arguments?.getParcelable("selectedCrypto")

        // Set the data to TextViews
        binding.tvFragmentCryptoDetailName.text = selectedCrypto!!.name
        binding.tvFragmentCryptoDetailSymbol.text = selectedCrypto!!.symbol!!.uppercase()
        binding.tvFragmentCryptoDetailRank.text =
            "#${selectedCrypto!!.market_cap_rank}"
        binding.tvFragmentCryptoDetailPriceChangePercentage24h.text =
            selectedCrypto!!.price_change_percentage_24h.customFormattedPercentage()
        if (selectedCrypto!!.price_change_percentage_24h >= 0) {
            binding.tvFragmentCryptoDetailPriceChangePercentage24h.positivePrice()
        } else {
            binding.tvFragmentCryptoDetailPriceChangePercentage24h.negativePrice()
        }
        binding.tvFragmentCryptoDetailPriceChange24h.text = run {
            if (selectedCrypto!!.price_change_24h >= 0) {
                binding.tvFragmentCryptoDetailPriceChange24h.positivePrice()
                "+${selectedCrypto!!.price_change_24h.customFormattedPrice(userCurrency)}"
            } else {
                binding.tvFragmentCryptoDetailPriceChange24h.negativePrice()
                selectedCrypto!!.price_change_24h.customFormattedPrice(userCurrency)
            }
        }
        binding.tvFragmentCryptoDetailPrice.text =
            selectedCrypto!!.current_price.customFormattedPrice(userCurrency)

        val lineData = arrayListOf<Entry>()
        for (i in selectedCrypto!!.sparkline_in_7d!!.price.indices) {
            lineData.add(Entry(i.toFloat(), selectedCrypto!!.sparkline_in_7d!!.price[i].toFloat()))
        }

        val lds = LineDataSet(lineData, "LineData")
        // Change chart line color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lds.color = mResources.getColor(R.color.purple_app_accent, null)
        } else {
            lds.color = mResources.getColor(R.color.purple_app_accent)
        }
        // Change chart line width
        lds.lineWidth = 2F
        // Remove chart circles
        lds.setDrawCircles(false)
        lds.setDrawFilled(true)
        lds.fillDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.gradient_background_chart)

        val ld = LineData(arrayListOf(lds) as List<ILineDataSet>?)
        binding.bcFragmentCryptoDetailLine.data = ld
        // Remove legend
        binding.bcFragmentCryptoDetailLine.legend.isEnabled = false
        // Remove description
        binding.bcFragmentCryptoDetailLine.description.isEnabled = false
        // Remove axis left and right legend
        binding.bcFragmentCryptoDetailLine.axisLeft.isEnabled = false
        binding.bcFragmentCryptoDetailLine.axisRight.isEnabled = false
        // Remove xAxis chart-lines
        binding.bcFragmentCryptoDetailLine.xAxis.isEnabled = false
        // Animate chart when loads
        binding.bcFragmentCryptoDetailLine.animateX(375)
        // Disable double tap to zoom
        binding.bcFragmentCryptoDetailLine.isDoubleTapToZoomEnabled = false
        // Disable both axis scale
        binding.bcFragmentCryptoDetailLine.isScaleYEnabled = false
        binding.bcFragmentCryptoDetailLine.isScaleXEnabled = false
        // Change chart offsets
        binding.bcFragmentCryptoDetailLine.setViewPortOffsets(25F, 0F, 25F, 50F)
        // Max zoom
        binding.bcFragmentCryptoDetailLine.viewPortHandler.setMaximumScaleX(3F)
        binding.bcFragmentCryptoDetailLine.setOnChartValueSelectedListener(this)
        binding.bcFragmentCryptoDetailLine.setOnTouchListener { _, _ ->
            binding.bcFragmentCryptoDetailLine.onTouchListener.setLastHighlighted(null)
            binding.bcFragmentCryptoDetailLine.highlightValue(null)
            binding.tvFragmentCryptoDetailPrice.text =
                selectedCrypto!!.current_price.customFormattedPrice(userCurrency)
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheetBehavior = bottomSheetDialog.behavior
            bottomSheetBehavior.isDraggable = true
            false
        }

        binding.tvFragmentCryptoDetailPriceHigh7d.text =
            selectedCrypto!!.sparkline_in_7d!!.price.maxOrNull()!!
                .customFormattedPrice(userCurrency)
        binding.tvFragmentCryptoDetailPriceLow7d.text =
            selectedCrypto!!.sparkline_in_7d!!.price.minOrNull()!!
                .customFormattedPrice(userCurrency)

        return binding.root
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.isDraggable = false
        binding.tvFragmentCryptoDetailPrice.text = h!!.y.toDouble().customFormattedPrice(userCurrency)
    }

    override fun onNothingSelected() {

    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }
}