package com.kotlin.cryptofication.adapter

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.model.CryptoAlert
import com.kotlin.cryptofication.data.repos.CryptoAlertRepository
import com.kotlin.cryptofication.data.repos.CryptoProvider
import com.kotlin.cryptofication.databinding.AdapterCryptoBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.utilities.*
import kotlinx.coroutines.*
import javax.inject.Inject

class CryptoListAlertsAdapter @Inject constructor(
    private val cryptoProvider: CryptoProvider,
    private val mRoom: CryptoAlertRepository
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable,
    ITHSwipe {

    private var cryptoList: ArrayList<Any> = ArrayList()
    private var cryptoListFull: ArrayList<Any> = ArrayList()

    private val viewTypeCrypto = 0
    private val viewTypeBannerAd = 1

    private var onCryptoListAlertsListener: OnCryptoListAlertsListener? = null

    interface OnCryptoListAlertsListener {
        fun onCryptoClicked(bundle: Bundle)
        fun onCryptoEmptied(isEmpty: Boolean)
        fun onSnackbarCreated(snackbar: Snackbar)
        fun onAlertChanged()
    }

    fun setOnCryptoListAlertsListener(listener: OnCryptoListAlertsListener?) {
        onCryptoListAlertsListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            viewTypeCrypto -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_crypto, parent, false)
                CryptoListAlertsViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_banner_ad, parent, false)
                AdBannerListAlertsViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            viewTypeCrypto -> {
                val cryptoHolder = holder as CryptoListAlertsViewHolder
                if (cryptoList[position] is Crypto) {
                    val selectedCrypto = cryptoList[position] as Crypto
                    cryptoHolder.bind(selectedCrypto)
                    cryptoHolder.bindingCrypto.parentLayoutCrypto.setOnClickListener {
                        val bundle = bundleOf("selectedCrypto" to selectedCrypto)
                        onCryptoListAlertsListener?.onCryptoClicked(bundle)
                    }
                }
            }
            else -> {
                val bannerHolder = holder as AdBannerListAlertsViewHolder
                if (cryptoList[position] is AdView) {
                    val adView = cryptoList[position] as AdView
                    val adBannerView = bannerHolder.itemView as ViewGroup
                    if (adBannerView.childCount > 0) {
                        adBannerView.removeAllViews()
                    }
                    if (adView.parent != null) {
                        (adView.parent as ViewGroup).removeView(adView)
                    }

                    // Add the banner ad to the ad view.
                    adBannerView.addView(adView)
                }
            }
        }
    }

    override fun getItemCount() = cryptoList.size

    override fun getItemViewType(position: Int): Int =
        if (position % Constants.ITEMS_PER_AD == 0 || position == 0) viewTypeBannerAd else viewTypeCrypto

    override fun getFilter() = filter

    private val filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filteredList = ArrayList<Any>()
            val query = charSequence.toString()

            if (query.isEmpty()) {
                filteredList.addAll(cryptoListFull)
            } else {
                val filterPattern = query.lowercase().trim { it <= ' ' }
                for ((i, item) in cryptoListFull.withIndex()) {
                    if (i == 0) {
                        filteredList.add(item)
                        continue
                    }
                    if (getItemViewType(i) == viewTypeCrypto) {
                        if ((item as Crypto).symbol.lowercase().contains(filterPattern) or
                            item.name.lowercase().contains(filterPattern)
                        ) filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        @Suppress("UNCHECKED_CAST")
        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            cryptoList.clear()
            cryptoList.addAll(filterResults.values as ArrayList<*>)
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCryptos(cryptoList: List<Any>) {
        this.cryptoList = ArrayList(cryptoList)
        this.cryptoListFull = ArrayList(cryptoList)
        notifyDataSetChanged()
    }

    override fun onItemSwiped(direction: Int, viewHolder: RecyclerView.ViewHolder) {
        // Get the position and the crypto symbol of the item
        val position = viewHolder.bindingAdapterPosition
        if (getItemViewType(position) == viewTypeBannerAd) return
        val crypto = cryptoList[position] as Crypto
        val cryptoId = crypto.id
        val cryptoSymbol = crypto.symbol.uppercase()

        // Add the item to the database, at the Favorites table (cryptoSymbol and the current date)
        CoroutineScope(Dispatchers.Default).launch {
            val cryptoSwiped = CryptoAlert(cryptoId, cryptoSymbol, crypto.current_price, 0.0)
            val savedAlerts = withContext(Dispatchers.IO){ mRoom.getAllAlerts()}.size

            when (withContext(Dispatchers.IO){ mRoom.deleteAlert(cryptoSwiped) }) {
                0 -> {
                    // The item wasn't in the database
                    cryptoList.removeAt(position)
                    notifyItemRemoved(position)
                    cryptoProvider.cryptosAlerts = cleanAdsCryptoList(cryptoList)
                    onCryptoListAlertsListener?.onAlertChanged()
                    if (cryptoList.size == 0) {
                        onCryptoListAlertsListener?.onCryptoEmptied(true)
                    }
                    Snackbar
                        .make(
                            viewHolder.itemView,
                            "$cryptoSymbol couldn't be removed, updating list...",
                            Snackbar.LENGTH_LONG
                        ).let { snackbar ->
                            onCryptoListAlertsListener?.onSnackbarCreated(snackbar)
                        }
                }
                else -> {
                    // The item has been deleted from the database successfully. Add the action to undo the action
                    cryptoList.removeAt(position)
                    notifyItemRemoved(position)
                    cryptoProvider.cryptosAlerts = cleanAdsCryptoList(cryptoList)
                    onCryptoListAlertsListener?.onAlertChanged()
                    if (savedAlerts == 1) {
                        mAlarmManager.deleteAlarmManager()
                        onCryptoListAlertsListener?.onCryptoEmptied(true)
                        mPrefs.setDBHasItems(false)
                    }

                    Snackbar
                        .make(
                            viewHolder.itemView,
                            "$cryptoSymbol removed from favorites",
                            Snackbar.LENGTH_SHORT
                        )
                        .setAction("UNDO") {
                            CoroutineScope(Dispatchers.Default).launch {
                                // When undo is clicked, delete the item from table Favorites
                                val resultInsert: Int = try {
                                    withContext(Dispatchers.IO){ mRoom.insertAlert(cryptoSwiped)}.toInt()
                                } catch (e: SQLiteConstraintException) {
                                    e.printStackTrace()
                                    0
                                }

                                resultInsert.let {
                                    when {
                                        it == 0 ->
                                            // The item couldn't be deleted
                                            withContext(Dispatchers.Main){ mAppContext.showToast("$cryptoSymbol already in favorites") }
                                        it > 0 -> {
                                            // The item has been deleted successfully
                                            withContext(Dispatchers.Main){ mAppContext.showToast("$cryptoSymbol added to favorites") }
                                            cryptoList.add(position, crypto)
                                            notifyItemInserted(position)
                                            cryptoProvider.cryptosAlerts =
                                                cleanAdsCryptoList(cryptoList)
                                            onCryptoListAlertsListener?.onAlertChanged()
                                            if (savedAlerts == 1) {
                                                mAlarmManager.launchAlarmManager()
                                                onCryptoListAlertsListener?.onCryptoEmptied(false)
                                                mPrefs.setDBHasItems(true)
                                            }
                                        }
                                    }
                                }
                            }
                        }.let { snackbar ->
                            onCryptoListAlertsListener?.onSnackbarCreated(snackbar)
                        }
                }
            }
        }
    }

    private fun cleanAdsCryptoList(cryptoList: ArrayList<Any>): List<Crypto> {
        val cleanCryptoList = ArrayList<Crypto>()
        for (item in cryptoList) {
            if (item is Crypto) {
                cleanCryptoList.add(item)
            }
        }
        return cleanCryptoList
    }

    fun goToCrypto(cryptoId: String) {
        for (item in cryptoList) {
            if (item is Crypto) {
                if (item.id == cryptoId) {
                    val bundle = bundleOf("selectedCrypto" to item)
                    onCryptoListAlertsListener?.onCryptoClicked(bundle)
                }
            }
        }
    }

    class CryptoListAlertsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bindingCrypto = AdapterCryptoBinding.bind(itemView)
        private val userCurrency = mPrefs.getCurrencySymbol()
        private val circularProgressDrawable = CircularProgressDrawable(itemView.context).apply {
            setColorSchemeColors(R.color.purple_app_accent)
            backgroundColor = R.color.text
            strokeWidth = 10f
            start()
        }

        fun bind(crypto: Crypto) {
            bindingCrypto.apply {
                Glide.with(itemView).load(crypto.image).diskCacheStrategy(
                    DiskCacheStrategy.AUTOMATIC
                ).placeholder(circularProgressDrawable).override(0, 35)
                    .into(ivAdapterCryptoIcon)
                tvAdapterCryptoSymbol.text = crypto.symbol.uppercase()
                tvAdapterCryptoName.text = crypto.name
                val currentPrice = crypto.current_price.customFormattedPrice(userCurrency)
                tvAdapterCryptoPrice.text = currentPrice
                val priceChange = crypto.price_change_percentage_24h.customFormattedPercentage()
                tvAdapterCryptoTextPriceChange.text = priceChange
                if (crypto.price_change_percentage_24h >= 0) {
                    ivAdapterCryptoIconPriceChange.positivePrice()
                    tvAdapterCryptoTextPriceChange.positivePrice()
                } else {
                    ivAdapterCryptoIconPriceChange.negativePrice()
                    tvAdapterCryptoTextPriceChange.negativePrice()
                }
            }
        }
    }

    class AdBannerListAlertsViewHolder(view: View) : RecyclerView.ViewHolder(view)
}