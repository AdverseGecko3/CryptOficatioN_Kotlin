package com.kotlin.cryptofication.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.data.model.CryptoAlert
import com.kotlin.cryptofication.databinding.AdapterPortfolioBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.utilities.customFormattedPrice
import com.kotlin.cryptofication.utilities.formattedDouble
import com.kotlin.cryptofication.utilities.showToast
import java.lang.NumberFormatException
import java.text.NumberFormat
import java.util.*

class CryptoListAlertsPortfolioAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var alertsList: ArrayList<CryptoAlert> = ArrayList()
    private var totalList = mutableListOf<Double>()

    private var onCryptoListAlertsPortfolioListener: OnCryptoListAlertsPortfolioListener? = null

    interface OnCryptoListAlertsPortfolioListener {
        fun onQuantityUpdatedCrypto(cryptoAlert: CryptoAlert)
        fun onQuantityUpdatedTotal(total: Double)
    }

    fun setOnCryptoListAlertsPortfolioListener(listener: OnCryptoListAlertsPortfolioListener?) {
        onCryptoListAlertsPortfolioListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_portfolio, parent, false)
        return CryptoListAlertsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cryptoHolder = holder as CryptoListAlertsViewHolder
        val selectedCrypto = alertsList[position]
        updateTotal(selectedCrypto)
        cryptoHolder.bind(selectedCrypto)
        cryptoHolder.bindingCrypto.etAdapterPortfolioQuantity.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val etText = cryptoHolder.bindingCrypto.etAdapterPortfolioQuantity
                val format = NumberFormat.getInstance(Locale.getDefault())
                val quantity = try {
                    etText.text.toString().toDouble()
                } catch (e: NumberFormatException) {
                    format.parse(etText.text.toString())!!.toDouble()
                }
                when {
                    etText.text.isEmpty() -> {
                        etText.setText("0")
                        updateQuantity(cryptoHolder, quantity, selectedCrypto, position)
                    }
                    quantity < 0 -> {
                        v.context.showToast("Quantity must be greater than 0!")
                    }
                    else -> {
                        etText.setText(
                            quantity.formattedDouble()
                        )
                        updateQuantity(cryptoHolder, quantity, selectedCrypto, position)
                        etText.clearFocus()
                    }
                }
            }
            false
        }
        if (position + 1 == itemCount) {
            onCryptoListAlertsPortfolioListener?.onQuantityUpdatedTotal(getTotal())
        }
    }

    private fun updateQuantity(
        cryptoHolder: CryptoListAlertsViewHolder,
        newQuantity: Double,
        selectedCrypto: CryptoAlert,
        position: Int
    ) {
        val newCryptoPortfolioPrice = (newQuantity * selectedCrypto.current_price)
        cryptoHolder.bindingCrypto.tvAdapterPortfolioPrice.text =
            newCryptoPortfolioPrice.customFormattedPrice(cryptoHolder.userCurrency, true)
        updateTotal(selectedCrypto, newQuantity, position)
        selectedCrypto.quantity = newQuantity
        onCryptoListAlertsPortfolioListener?.onQuantityUpdatedCrypto(selectedCrypto)
        onCryptoListAlertsPortfolioListener?.onQuantityUpdatedTotal(getTotal())
    }

    override fun getItemCount() = alertsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setCryptos(alertsList: ArrayList<CryptoAlert>) {
        this.alertsList = alertsList
        totalList = arrayListOf()
        notifyDataSetChanged()
    }

    private fun updateTotal(cryptoAlert: CryptoAlert, newQuantity: Double = 0.0, pos: Int = -1) {
        if (pos == -1) {
            val cryptoPortfolioPrice = (cryptoAlert.quantity * cryptoAlert.current_price)
            totalList.add(cryptoPortfolioPrice)
        } else {
            val cryptoPortfolioPrice = (newQuantity * cryptoAlert.current_price)
            totalList[pos] = cryptoPortfolioPrice
        }
    }

    private fun getTotal(): Double {
        var total = 0.0
        totalList.forEach {
            total += it
        }
        return total
    }

    class CryptoListAlertsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bindingCrypto = AdapterPortfolioBinding.bind(itemView)
        val userCurrency = mPrefs.getCurrencySymbol()

        fun bind(crypto: CryptoAlert) {
            bindingCrypto.apply {
                tvAdapterPortfolioSymbol.text = crypto.symbol.uppercase()
                etAdapterPortfolioQuantity.setText(crypto.quantity.formattedDouble())
                tvAdapterPortfolioPrice.text =
                    (crypto.quantity * crypto.current_price).customFormattedPrice(userCurrency, true)

            }
        }
    }
}