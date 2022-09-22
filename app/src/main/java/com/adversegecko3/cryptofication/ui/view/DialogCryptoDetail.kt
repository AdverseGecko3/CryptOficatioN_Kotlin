package com.adversegecko3.cryptofication.ui.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.databinding.DialogCryptoDetailBinding
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.adversegecko3.cryptofication.utilities.*
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogCryptoDetail : BottomSheetDialogFragment(), OnChartValueSelectedListener {
    private var _binding: DialogCryptoDetailBinding? = null
    private val binding get() = _binding!!
    private val userCurrency = mPrefs.getCurrencySymbol()

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCryptoDetailBinding.inflate(layoutInflater, container, false)

        val selectedCrypto: Crypto? = arguments?.parcelable("selectedCrypto")

        binding.apply {
            // Set the data to TextViews
            tvFragmentCryptoDetailName.text = selectedCrypto!!.name
            tvFragmentCryptoDetailSymbol.text = selectedCrypto.symbol.uppercase()
            tvFragmentCryptoDetailRank.text =
                "#${selectedCrypto.market_cap_rank}"
            tvFragmentCryptoDetailPriceChangePercentage24h.text =
                selectedCrypto.price_change_percentage_24h.customFormattedPercentage()
            if (selectedCrypto.price_change_percentage_24h >= 0) {
                tvFragmentCryptoDetailPriceChangePercentage24h.positivePrice()
            } else {
                tvFragmentCryptoDetailPriceChangePercentage24h.negativePrice()
            }
            tvFragmentCryptoDetailPriceChange24h.text = run {
                if (selectedCrypto.price_change_24h >= 0) {
                    tvFragmentCryptoDetailPriceChange24h.positivePrice()
                    "+${selectedCrypto.price_change_24h.customFormattedPrice(userCurrency)}"
                } else {
                    tvFragmentCryptoDetailPriceChange24h.negativePrice()
                    selectedCrypto.price_change_24h.customFormattedPrice(userCurrency)
                }
            }
            tvFragmentCryptoDetailPrice.text =
                selectedCrypto.current_price.customFormattedPrice(userCurrency)
        }

        val lineData = arrayListOf<Entry>()
        for (i in selectedCrypto!!.sparkline_in_7d?.price!!.indices) {
            lineData.add(Entry(i.toFloat(), selectedCrypto.sparkline_in_7d!!.price[i].toFloat()))
        }

        if (lineData.isEmpty()) {
            binding.apply {
                bcFragmentCryptoDetailLine.visibility = View.GONE
                tvFragmentCryptoDetailPriceHigh7d.visibility = View.GONE
                tvFragmentCryptoDetailPriceLow7d.visibility = View.GONE
                tvFragmentCryptoDetailNoData.visibility = View.VISIBLE
            }
        } else {
            val lds = LineDataSet(lineData, "LineData").apply {
                color = if (SDK_INT >= Build.VERSION_CODES.M) {
                    resources.getColor(R.color.purple_app_accent, null)
                } else {
                    @Suppress("DEPRECATION")
                    resources.getColor(R.color.purple_app_accent)
                }
                // Change chart line width
                lineWidth = 2F
                // Remove chart circles
                setDrawCircles(false)
                setDrawFilled(true)
                fillDrawable =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.gradient_background_chart
                    )
            }

            val ld = LineData(arrayListOf(lds) as List<ILineDataSet>?)
            binding.apply {
                bcFragmentCryptoDetailLine.apply {
                    data = ld
                    legend.isEnabled = false
                    // Remove description
                    description.isEnabled = false
                    // Remove axis left and right legend
                    axisLeft.isEnabled = false
                    axisRight.isEnabled = false
                    // Remove xAxis chart-lines
                    xAxis.isEnabled = false
                    // Animate chart when loads
                    animateX(375)
                    // Disable double tap to zoom
                    isDoubleTapToZoomEnabled = false
                    // Disable both axis scale
                    isScaleYEnabled = false
                    isScaleXEnabled = false
                    // Change chart offsets
                    setViewPortOffsets(25F, 0F, 25F, 50F)
                    // Max zoom
                    viewPortHandler.setMaximumScaleX(3F)
                    setOnChartValueSelectedListener(this@DialogCryptoDetail)
                    setOnTouchListener { _, _ ->
                        onTouchListener.setLastHighlighted(null)
                        highlightValue(null)
                        tvFragmentCryptoDetailPrice.text =
                            selectedCrypto.current_price.customFormattedPrice(userCurrency)
                        val bottomSheetDialog = dialog as BottomSheetDialog
                        val bottomSheetBehavior = bottomSheetDialog.behavior
                        bottomSheetBehavior.isDraggable = true
                        false
                    }
                }

                selectedCrypto.sparkline_in_7d?.price.also {
                    tvFragmentCryptoDetailPriceHigh7d.text =
                        it?.maxOrNull()?.customFormattedPrice(userCurrency)
                    tvFragmentCryptoDetailPriceLow7d.text =
                        it?.minOrNull()?.customFormattedPrice(userCurrency)
                }
            }
        }

        return binding.root
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.isDraggable = false
        binding.tvFragmentCryptoDetailPrice.text =
            h!!.y.toDouble().customFormattedPrice(userCurrency)
    }

    override fun onNothingSelected() {

    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }
}