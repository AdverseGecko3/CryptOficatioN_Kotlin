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
import com.google.android.material.snackbar.Snackbar
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.model.CryptoAlert
import com.kotlin.cryptofication.data.repos.CryptoProvider
import com.kotlin.cryptofication.databinding.AdapterAlertCryptoListBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mRoom
import com.kotlin.cryptofication.utilities.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CryptoListAlertsAdapter :
    RecyclerView.Adapter<CryptoListAlertsAdapter.CryptoListAlertsViewHolder>(),
    Filterable,
    ITHSwipe {

    private var cryptoList: ArrayList<Crypto> = ArrayList()
    private var cryptoListFull: ArrayList<Crypto> = ArrayList()

    private var onCryptoClickListener: OnCryptoClickListener? = null
    private var onCryptoEmptyListener: OnCryptoEmptyListener? = null
    private var onSnackbarCreatedListener: OnSnackbarCreatedLister? = null

    interface OnCryptoClickListener {
        fun onCryptoClicked(bundle: Bundle)
    }

    interface OnCryptoEmptyListener {
        fun onCryptoEmptied(isEmpty: Boolean)
    }

    interface OnSnackbarCreatedLister {
        fun onSnackbarCreated(snackbar: Snackbar)
    }

    fun setOnCryptoClickListener(listener: OnCryptoClickListener?) {
        onCryptoClickListener = listener
    }

    fun setOnCryptoEmptyListener(listener: OnCryptoEmptyListener?) {
        onCryptoEmptyListener = listener
    }

    fun setOnSnackbarCreatedListener(listener: OnSnackbarCreatedLister?) {
        onSnackbarCreatedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoListAlertsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_alert_crypto_list, parent, false)
        return CryptoListAlertsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoListAlertsViewHolder, position: Int) {
        val selectedCrypto = cryptoList[position]
        holder.bind(selectedCrypto)
        holder.binding.parentLayoutAlerts.setOnClickListener {
            val bundle = bundleOf("selectedCrypto" to selectedCrypto)
            onCryptoClickListener?.onCryptoClicked(bundle)
        }
    }

    override fun getItemCount() = cryptoList.size

    override fun getFilter() = filter

    private val filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filteredList = ArrayList<Crypto>()
            val query = charSequence.toString()

            if (query.isEmpty()) {
                Log.d("performFilter", "Filter empty")
                filteredList.addAll(cryptoListFull)
            } else {
                Log.d("performFilter", "Filter not empty")
                val filterPattern = query.lowercase().trim { it <= ' ' }
                Log.d("performFilter", filterPattern)
                for (crypto in cryptoListFull) {
                    if (crypto.name!!.lowercase().contains(filterPattern)) {
                        filteredList.add(crypto)
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
        @Suppress("UNCHECKED_CAST")
        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            cryptoList.clear()
            cryptoList.addAll(filterResults.values as ArrayList<Crypto>)
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCryptos(cryptoList: List<Crypto>) {
        this.cryptoList = ArrayList(cryptoList)
        this.cryptoListFull = ArrayList(cryptoList)
        notifyDataSetChanged()
    }

    override fun onItemSwiped(direction: Int, viewHolder: RecyclerView.ViewHolder) {
        // Get the position and the crypto symbol of the item
        val position = viewHolder.bindingAdapterPosition
        val crypto = cryptoList[position]
        Log.d("itemSwipe", "Crypto: $crypto")
        val cryptoId = crypto.id
        val cryptoSymbol = crypto.symbol?.uppercase()
        Log.d("itemSwipe", "Item position: $position - Item symbol: $cryptoSymbol")

        // Add the item to the database, at the Favorites table (cryptoSymbol and the current date)
        MainScope().launch {
            val cryptoSwiped = CryptoAlert(cryptoId!!)
            val savedAlerts = mRoom.getAllAlerts().size
            Log.d("itemSwipe", "Alerts Size: $savedAlerts")
            val resultDelete: Int = mRoom.deleteAlert(cryptoSwiped)
            Log.d("itemSwipe", "ResultDelete: $resultDelete")

            when (resultDelete) {
                0 -> {
                    // The item wasn't in the database
                    cryptoList.removeAt(position)
                    notifyItemRemoved(position)
                    CryptoProvider.cryptosAlerts = cryptoList
                    if (cryptoList.size == 0) {
                        onCryptoEmptyListener?.onCryptoEmptied(true)
                    }
                    Snackbar
                        .make(
                            viewHolder.itemView,
                            "$cryptoSymbol couldn't be removed, updating list...",
                            Snackbar.LENGTH_LONG
                        ).let { snackbar ->
                            onSnackbarCreatedListener?.onSnackbarCreated(snackbar)
                        }
                }
                else -> {
                    // The item has been deleted from the database successfully. Add the action to undo the action
                    cryptoList.removeAt(position)
                    notifyItemRemoved(position)
                    CryptoProvider.cryptosAlerts = cryptoList
                    if (savedAlerts == 1) {
                        Log.d("savedAlerts", "Delete Alarm Manager")
                        mAlarmManager.deleteAlarmManager()
                        onCryptoEmptyListener?.onCryptoEmptied(true)
                        mPrefs.setDBHasItems(false)
                    }

                    Snackbar
                        .make(
                            viewHolder.itemView,
                            "$cryptoSymbol removed from favorites",
                            Snackbar.LENGTH_SHORT
                        )
                        .setAction("UNDO") {
                            MainScope().launch {
                                // When undo is clicked, delete the item from table Favorites
                                val resultInsert: Int = try {
                                    mRoom.insertAlert(cryptoSwiped).toInt()
                                } catch (e: SQLiteConstraintException) {
                                    e.printStackTrace()
                                    0
                                }
                                Log.d("itemSwipe", "ResultInsert: $resultInsert")

                                resultInsert.let {
                                    when {
                                        it == 0 ->
                                            // The item couldn't be deleted
                                            mAppContext.showToast("$cryptoSymbol already in favorites")
                                        it > 0 -> {
                                            // The item has been deleted successfully
                                            mAppContext.showToast("$cryptoSymbol added to favorites")
                                            Log.d("itemSwipe", "Crypto: $crypto")
                                            cryptoList.add(position, crypto)
                                            notifyItemInserted(position)
                                            CryptoProvider.cryptosAlerts = cryptoList
                                            if (savedAlerts == 1) {
                                                Log.d("savedAlerts", "Launch Alarm Manager again")
                                                mAlarmManager.launchAlarmManager()
                                                onCryptoEmptyListener?.onCryptoEmptied(false)
                                                mPrefs.setDBHasItems(true)
                                            }
                                        }
                                    }
                                }
                            }
                        }.let { snackbar ->
                            onSnackbarCreatedListener?.onSnackbarCreated(snackbar)
                        }
                }
            }
        }
    }

    fun goToCrypto(cryptoId: String) {
        for (crypto in cryptoList) {
            if (crypto.id == cryptoId) {
                val bundle = bundleOf("selectedCrypto" to crypto)
                onCryptoClickListener?.onCryptoClicked(bundle)
            }
        }
    }

    class CryptoListAlertsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val binding = AdapterAlertCryptoListBinding.bind(itemView)
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
                .into(binding.ivAdapterAlertIcon)
            binding.tvAdapterAlertSymbol.text = crypto.symbol!!.uppercase()
            binding.tvAdapterAlertName.text = crypto.name
            val currentPrice = crypto.current_price.customFormattedPrice(userCurrency)
            binding.tvAdapterAlertPrice.text = currentPrice
            val priceChange = crypto.price_change_percentage_24h.customFormattedPercentage()
            binding.tvAdapterAlertTextPriceChange.text = priceChange
            if (crypto.price_change_percentage_24h >= 0) {
                binding.ivAdapterAlertIconPriceChange.positivePrice()
                binding.tvAdapterAlertTextPriceChange.positivePrice()
            } else {
                binding.ivAdapterAlertIconPriceChange.negativePrice()
                binding.tvAdapterAlertTextPriceChange.negativePrice()
            }
        }
    }
}