package com.adversegecko3.cryptofication.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.data.model.CryptoAlert
import com.adversegecko3.cryptofication.databinding.AdapterPortfolioBinding
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.adversegecko3.cryptofication.utilities.customFormattedPrice
import com.adversegecko3.cryptofication.utilities.formattedDouble
import com.adversegecko3.cryptofication.utilities.showToast

class CryptoListAlertsPortfolioAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var alertsList: ArrayList<CryptoAlert> = ArrayList()
    private var totalList = mutableListOf<Double>()

    private var onCryptoListAlertsPortfolioListener: OnCryptoListAlertsPortfolioListener? = null

    interface OnCryptoListAlertsPortfolioListener {
        fun onQuantityUpdatedCrypto(cryptoAlert: CryptoAlert)
        fun onQuantityUpdatedTotal(total: Double, bitcoinPrice: Double)
    }

    fun setOnCryptoListAlertsPortfolioListener(listener: OnCryptoListAlertsPortfolioListener?) {
        onCryptoListAlertsPortfolioListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_portfolio, parent, false)
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
                val quantity = try {
                    etText.text.toString().toDouble()
                } catch (e: NumberFormatException) {
                    Log.e("NumberFormatExc", e.message.toString())
                    0.0
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
                        etText.setText(quantity.formattedDouble())
                        updateQuantity(cryptoHolder, quantity, selectedCrypto, position)
                    }
                }
                etText.clearFocus()
            }
            false
        }
        if (position + 1 == itemCount) {
            onCryptoListAlertsPortfolioListener?.onQuantityUpdatedTotal(
                getTotal(),
                alertsList[itemCount].current_price
            )
        }
    }

    override fun getItemCount() = (alertsList.size - 1)

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
        onCryptoListAlertsPortfolioListener?.onQuantityUpdatedTotal(
            getTotal(), alertsList[itemCount].current_price
        )
    }

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
                    (crypto.quantity * crypto.current_price).customFormattedPrice(
                        userCurrency, true
                    )

            }
        }
    }
}