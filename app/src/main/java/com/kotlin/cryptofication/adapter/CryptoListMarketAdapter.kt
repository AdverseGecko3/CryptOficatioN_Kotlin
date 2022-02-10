package com.kotlin.cryptofication.adapter

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
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
import com.kotlin.cryptofication.databinding.AdapterMarketCryptoListBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mRoom
import com.kotlin.cryptofication.utilities.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CryptoListMarketAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Filterable,
    ITHSwipe {

    private var cryptoList: ArrayList<Any> = ArrayList()
    private var cryptoListFull: ArrayList<Any> = ArrayList()

    private val viewTypeCrypto = 0
    private val viewTypeBannerAd = 1

    private var onCryptoClickedLister: OnCryptoClickedListener? = null
    private var onSnackbarCreatedLister: OnSnackbarCreatedLister? = null

    interface OnCryptoClickedListener {
        fun onCryptoClicked(bundle: Bundle)
    }

    interface OnSnackbarCreatedLister {
        fun onSnackbarCreated(snackbar: Snackbar)
    }

    fun setOnCryptoClickListener(listener: OnCryptoClickedListener?) {
        onCryptoClickedLister = listener
    }

    fun setOnSnackbarCreatedListener(listener: OnSnackbarCreatedLister?) {
        onSnackbarCreatedLister = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            viewTypeCrypto -> {
                Log.d("onCreateViewHolder", "is viewTypeCrypto")
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_market_crypto_list, parent, false)
                CryptoListMarketViewHolder(view)
            }
            else -> {
                Log.d("onCreateViewHolder", "is viewTypeBannerAd")
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_market_banner_ad, parent, false)
                AdBannerListMarketViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            viewTypeCrypto -> {
                val cryptoHolder = holder as CryptoListMarketViewHolder
                if (cryptoList[position] is Crypto) {
                    Log.d("onBindViewHolder", "is Crypto")
                    val selectedCrypto = cryptoList[position] as Crypto
                    Log.d("onBindViewHolder", "is viewTypeCrypto - ${selectedCrypto.symbol}")
                    cryptoHolder.bind(selectedCrypto)
                    cryptoHolder.bindingCrypto.parentLayoutMarket.setOnClickListener {
                        val bundle = bundleOf("selectedCrypto" to selectedCrypto)
                        onCryptoClickedLister?.onCryptoClicked(bundle)
                    }
                }
            }
            else -> {
                Log.d("onBindViewHolder", "is viewTypeBannerAd")
                val bannerHolder = holder as AdBannerListMarketViewHolder
                if (cryptoList[position] is AdView) {
                    Log.d("onBindViewHolder", "is AdView")
                    val adView = cryptoList[position] as AdView
                    val adBannerView = bannerHolder.itemView as ViewGroup
                    if (adBannerView.childCount > 0) {
                        Log.d("AdBannerListMarketVH", "childCount > 0")
                        adBannerView.removeAllViews()
                    }
                    if (adView.parent != null) {
                        Log.d("AdBannerListMarketVH", "adview parent != null")
                        (adView.parent as ViewGroup).removeView(adView)
                    }

                    // Add the banner ad to the ad view.
                    adBannerView.addView(adView)
                    Log.d("AdBannerListMarketVH", "added view")
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
            val filteredList: ArrayList<Any> = ArrayList()
            val query = charSequence.toString()

            if (query.isEmpty()) {
                Log.d("performFilter", "Filter empty")
                filteredList.addAll(cryptoListFull)
            } else {
                Log.d("performFilter", "Filter not empty")
                val filterPattern = query.lowercase().trim { it <= ' ' }
                Log.d("performFilter", filterPattern)
                for ((i, item) in cryptoListFull.withIndex()) {
                    if (i == 0) {
                        filteredList.add(item)
                        continue
                    }
                    if (getItemViewType(i) == viewTypeCrypto) {
                        if ((item as Crypto).symbol!!.lowercase().contains(filterPattern) or
                            item.name!!.lowercase().contains(filterPattern)
                        ) filteredList.add(item)
                    }
                }
            }
            Log.d(
                "performFilter",
                "List full:${cryptoList.size} List filter:${cryptoListFull.size}"
            )
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
        val cryptoSymbol = crypto.symbol?.uppercase()
        Log.d("itemSwipe", "Item position: $position - Item symbol: $cryptoSymbol")

        MainScope().launch {
            val savedAlerts: Int
            if (mRoom.getSingleAlert(cryptoId!!) == null) {
                val cryptoSwiped = CryptoAlert(cryptoId)
                savedAlerts = mRoom.getAllAlerts().size
                val resultInsert: Int = try {
                    mRoom.insertAlert(cryptoSwiped).toInt()
                } catch (e: SQLiteConstraintException) {
                    e.printStackTrace()
                    0
                }
                Log.d("itemSwipe", "ResultInsert: $resultInsert")

                resultInsert.let {
                    Log.d("itemSwipe", "it: $it")
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
                                    onSnackbarCreatedLister?.onSnackbarCreated(snackbar)
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
                        onSnackbarCreatedLister?.onSnackbarCreated(snackbar)
                    }
            }
        }
    }

    class CryptoListMarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bindingCrypto = AdapterMarketCryptoListBinding.bind(itemView)
        private val userCurrency = mPrefs.getCurrencySymbol()
        private val circularProgressDrawable = CircularProgressDrawable(itemView.context).apply {
            setColorSchemeColors(R.color.purple_app_accent)
            backgroundColor = R.color.text
            strokeWidth = 10f
            start()
        }

        fun bind(crypto: Crypto) {
            Glide.with(itemView).load(crypto.image).diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).placeholder(circularProgressDrawable).override(0, 35)
                .into(bindingCrypto.ivAdapterMarketIcon)
            bindingCrypto.tvAdapterMarketSymbol.text = crypto.symbol!!.uppercase()
            bindingCrypto.tvAdapterMarketName.text = crypto.name
            val currentPrice = crypto.current_price.customFormattedPrice(userCurrency)
            bindingCrypto.tvAdapterMarketPrice.text = currentPrice
            val priceChange = crypto.price_change_percentage_24h.customFormattedPercentage()
            bindingCrypto.tvAdapterMarketTextPriceChange.text = priceChange
            if (crypto.price_change_percentage_24h >= 0) {
                bindingCrypto.ivAdapterMarketIconPriceChange.positivePrice()
                bindingCrypto.tvAdapterMarketTextPriceChange.positivePrice()
            } else {
                bindingCrypto.ivAdapterMarketIconPriceChange.negativePrice()
                bindingCrypto.tvAdapterMarketTextPriceChange.negativePrice()
            }
        }
    }

    class AdBannerListMarketViewHolder(view: View) : RecyclerView.ViewHolder(view)
}