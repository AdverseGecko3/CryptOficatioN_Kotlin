package com.adversegecko3.cryptofication.adapter

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.model.CryptoAlert
import com.adversegecko3.cryptofication.data.repos.CryptoAlertRepository
import com.adversegecko3.cryptofication.databinding.AdapterCryptoBinding
import com.adversegecko3.cryptofication.databinding.AdapterLoadMoreBinding
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.adversegecko3.cryptofication.utilities.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CryptoListMarketAdapter @Inject constructor(private val mRoom: CryptoAlertRepository) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
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
        fun onLoadMoreClicked(loadType: Int)
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
                val loadType = if (position == 0) 0 else 1
                loadMoreHolder.bind(loadType)
                loadMoreHolder.bindingLoadMore.btnLoadMore.setOnClickListener {
                    onCryptoListMarketListener?.onLoadMoreClicked(loadType)
                }
            }
        }
    }

    override fun getItemCount() = cryptoList.size

    override fun getItemViewType(position: Int): Int {
        if (cryptoListFull[0] is Double) {
            if (position == cryptoListFull.lastIndex || position == 0) return viewTypeLoadMore
            return if (position % Constants.ITEMS_PER_AD == 0) viewTypeBannerAd else viewTypeCrypto
        } else {
            if (position == cryptoListFull.lastIndex) return viewTypeLoadMore
            return if (position % Constants.ITEMS_PER_AD == 0 && position != 0) viewTypeBannerAd else viewTypeCrypto
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
        if (getItemViewType(position) == (viewTypeBannerAd or viewTypeLoadMore)) return
        val crypto = cryptoList[position] as Crypto
        val cryptoId = crypto.id
        val cryptoSymbol = crypto.symbol.uppercase()

        CoroutineScope(Dispatchers.Default).launch {
            val savedAlerts: Int
            if (withContext(Dispatchers.IO) { mRoom.getSingleAlert(cryptoId) } == null) {
                val cryptoSwiped = CryptoAlert(cryptoId, cryptoSymbol)
                savedAlerts = withContext(Dispatchers.IO) { mRoom.getAllAlerts() }.size
                val resultInsert: Int = try {
                    withContext(Dispatchers.IO) { mRoom.insertAlert(cryptoSwiped) }.toInt()
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

                            try {
                                Snackbar
                                    .make(
                                        viewHolder.itemView,
                                        "$cryptoSymbol added to favorites",
                                        Snackbar.LENGTH_LONG
                                    )
                                    .setAction("UNDO") {
                                        CoroutineScope(Dispatchers.Default).launch {
                                            // When undo is clicked, delete the item from table Favorites
                                            when (mRoom.deleteAlert(cryptoSwiped)) {
                                                0 ->
                                                    // The item couldn't be deleted
                                                    withContext(Dispatchers.Main) {
                                                        mAppContext.showToast(
                                                            "$cryptoSymbol couldn't be removed"
                                                        )
                                                    }
                                                1 -> {
                                                    // The item has been deleted successfully
                                                    withContext(Dispatchers.Main) {
                                                        mAppContext.showToast(
                                                            "$cryptoSymbol removed from Alerts"
                                                        )
                                                    }

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
                            } catch (e: IllegalArgumentException) {
                                Log.e("IllegalArgumentExc", e.message.toString())
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

        fun bind(loadType: Int) {
            if (loadType == 0) {
                bindingLoadMore.btnLoadMore.apply {
                    text = resources.getString(R.string.PREVIOUS_PAGE)
                    icon =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_back, null)
                }
            } else {
                bindingLoadMore.btnLoadMore.apply {
                    text = resources.getString(R.string.NEXT_PAGE)
                    icon =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_forward, null)
                }
            }
        }
    }
}