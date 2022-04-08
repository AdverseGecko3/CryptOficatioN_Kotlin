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
import com.kotlin.cryptofication.databinding.AdapterCryptoBinding
import com.kotlin.cryptofication.databinding.AdapterLoadMoreBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.utilities.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CryptoListMarketAdapter @Inject constructor(private val mRoom: CryptoAlertRepository) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable,
    ITHSwipe {

    private var cryptoList: ArrayList<Any> = ArrayList()
    private var cryptoListFull: ArrayList<Any> = ArrayList()

    private val viewTypeCrypto = 0
    private val viewTypeBannerAd = 1
    private val viewTypeLoadMore = 2

    private var onCryptoListMarketListener: OnCryptoListMarketListener? = null

    interface OnCryptoListMarketListener {
        fun onCryptoClicked(bundle: Bundle)
        fun onSnackbarCreated(snackbar: Snackbar)
        fun onLoadMoreClicked()
    }

    fun setOnCryptoListMarketListener(listener: OnCryptoListMarketListener?) {
        onCryptoListMarketListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            viewTypeCrypto -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_crypto, parent, false)
                CryptoListMarketViewHolder(view)
            }
            viewTypeBannerAd -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_banner_ad, parent, false)
                AdBannerListMarketViewHolder(view)
            }
            viewTypeLoadMore -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_load_more, parent, false)
                LoadMoreListMarketViewHolder(view)
            }
            else -> throw ClassCastException("Type unknown")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            viewTypeCrypto -> {
                val cryptoHolder = holder as CryptoListMarketViewHolder
                if (cryptoList[position] is Crypto) {
                    val selectedCrypto = cryptoList[position] as Crypto
                    cryptoHolder.bind(selectedCrypto)
                    cryptoHolder.bindingCrypto.parentLayoutCrypto.setOnClickListener {
                        val bundle = bundleOf("selectedCrypto" to selectedCrypto)
                        onCryptoListMarketListener?.onCryptoClicked(bundle)
                    }
                }
            }
            viewTypeBannerAd -> {
                val bannerHolder = holder as AdBannerListMarketViewHolder
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
            viewTypeLoadMore -> {
                val loadMoreHolder = holder as LoadMoreListMarketViewHolder
                loadMoreHolder.bindingLoadMore.btnLoadMore.setOnClickListener {
                    onCryptoListMarketListener?.onLoadMoreClicked()
                }

            }
        }
    }

    override fun getItemCount() = cryptoList.size

    override fun getItemViewType(position: Int): Int {
        if (position == (cryptoListFull.lastIndex)) return viewTypeLoadMore
        return if (position % Constants.ITEMS_PER_AD == 0 || position == 0) viewTypeBannerAd else viewTypeCrypto
    }

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
                    if (getItemViewType(i) == viewTypeLoadMore) continue
                    if (i == 0) {
                        filteredList.add(item)
                        continue
                    }
                    if (getItemViewType(i) == viewTypeCrypto && item is Crypto) {
                        if (item.symbol.lowercase().contains(filterPattern) or
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

        MainScope().launch {
            val savedAlerts: Int
            if (mRoom.getSingleAlert(cryptoId) == null) {
                val cryptoSwiped = CryptoAlert(cryptoId, cryptoSymbol, crypto.current_price, 0.0)
                savedAlerts = mRoom.getAllAlerts().size
                val resultInsert: Int = try {
                    mRoom.insertAlert(cryptoSwiped).toInt()
                } catch (e: SQLiteConstraintException) {
                    e.printStackTrace()
                    0
                }

                resultInsert.let {
                    when {
                        it > 0 -> {
                            // The item has been added to the database successfully. Add the action to undo the action
                            notifyItemChanged(position)

                            if (savedAlerts == 0) {
                                mPrefs.setDBHasItems(true)
                                mAlarmManager.launchAlarmManager()
                            }

                            Snackbar
                                .make(
                                    viewHolder.itemView,
                                    "$cryptoSymbol added to favorites",
                                    Snackbar.LENGTH_LONG
                                )
                                .setAction("UNDO") {
                                    MainScope().launch {
                                        // When undo is clicked, delete the item from table Favorites
                                        when (mRoom.deleteAlert(cryptoSwiped)) {
                                            0 ->
                                                // The item couldn't be deleted
                                                mAppContext.showToast("$cryptoSymbol couldn't be removed")
                                            1 -> {
                                                // The item has been deleted successfully
                                                mAppContext.showToast("$cryptoSymbol removed from Alerts")

                                                if (savedAlerts == 0) {
                                                    mPrefs.setDBHasItems(false)
                                                    mAlarmManager.deleteAlarmManager()
                                                }
                                            }
                                        }
                                    }
                                }.let { snackbar ->
                                    onCryptoListMarketListener?.onSnackbarCreated(snackbar)
                                }

                        }
                        else -> {
                            // The item was already in the database
                            mAppContext.showToast("Error!")
                        }
                    }
                }
            } else {
                notifyItemChanged(position)
                Snackbar
                    .make(
                        viewHolder.itemView,
                        "$cryptoSymbol already in favorites",
                        Snackbar.LENGTH_SHORT
                    ).let { snackbar ->
                        onCryptoListMarketListener?.onSnackbarCreated(snackbar)
                    }
            }
        }
    }

    class CryptoListMarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

    class AdBannerListMarketViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class LoadMoreListMarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bindingLoadMore = AdapterLoadMoreBinding.bind(itemView)
    }
}