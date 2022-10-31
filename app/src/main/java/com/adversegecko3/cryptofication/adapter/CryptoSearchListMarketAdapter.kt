package com.adversegecko3.cryptofication.adapter

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.data.model.CryptoAlert
import com.adversegecko3.cryptofication.data.model.CryptoSearch
import com.adversegecko3.cryptofication.data.repos.CryptoAlertRepository
import com.adversegecko3.cryptofication.databinding.AdapterCryptoSearchBinding
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.adversegecko3.cryptofication.utilities.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CryptoSearchListMarketAdapter @Inject constructor(private val mRoom: CryptoAlertRepository) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), ITHSwipe {

    private var cryptoSearchList: ArrayList<CryptoSearch> = ArrayList()

    private var onCryptoSearchListMarketListener: OnCryptoSearchListMarketListener? = null

    interface OnCryptoSearchListMarketListener {
        fun onCryptoSearchClicked(selectedCrypto: CryptoSearch)
        fun onSnackbarCreated(snackbar: Snackbar)
    }

    fun setOnCryptoSearchListMarketListener(listener: OnCryptoSearchListMarketListener?) {
        onCryptoSearchListMarketListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_crypto_search, parent, false)
        return CryptoListMarketViewHolder(view)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cryptoHolder = holder as CryptoListMarketViewHolder
        val selectedCrypto = cryptoSearchList[position]
        cryptoHolder.bind(selectedCrypto)
        cryptoHolder.bindingCrypto.parentLayoutCrypto.setOnClickListener {
            onCryptoSearchListMarketListener?.onCryptoSearchClicked(selectedCrypto)
        }
    }

    override fun getItemCount() = cryptoSearchList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setCryptos(cryptoSearchList: List<CryptoSearch>) {
        this.cryptoSearchList = ArrayList(cryptoSearchList)
        notifyDataSetChanged()
    }

    override fun onItemSwiped(direction: Int, viewHolder: RecyclerView.ViewHolder) {
        // Get the position and the crypto symbol of the item
        val position = viewHolder.bindingAdapterPosition
        val crypto = cryptoSearchList[position]
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
                                        onCryptoSearchListMarketListener?.onSnackbarCreated(snackbar)
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
                        onCryptoSearchListMarketListener?.onSnackbarCreated(snackbar)
                    }
            }
        }
    }

    class CryptoListMarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bindingCrypto = AdapterCryptoSearchBinding.bind(itemView)
        private val circularProgressDrawable = CircularProgressDrawable(itemView.context).apply {
            setColorSchemeColors(R.color.purple_app_accent)
            backgroundColor = R.color.text
            strokeWidth = 10f
            start()
        }

        fun bind(crypto: CryptoSearch) {
            bindingCrypto.apply {
                Glide.with(itemView).load(crypto.thumb).diskCacheStrategy(
                    DiskCacheStrategy.AUTOMATIC
                ).placeholder(circularProgressDrawable).override(0, 35)
                    .into(ivAdapterCryptoSearchIcon)
                tvAdapterCryptoSearchSymbol.text = crypto.symbol.uppercase()
                tvAdapterCryptoSearchName.text = crypto.name
                val rank = "#${crypto.market_cap_rank}"
                tvAdapterCryptoSearchMarketCapRank.text = rank
            }
        }
    }
}