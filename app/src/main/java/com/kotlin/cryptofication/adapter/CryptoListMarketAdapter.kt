package com.kotlin.cryptofication.adapter

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.utilities.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlin.collections.ArrayList
import com.kotlin.cryptofication.data.model.CryptoAlert
import com.kotlin.cryptofication.databinding.AdapterMarketCryptoListBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mRoom

class CryptoListMarketAdapter :
    RecyclerView.Adapter<CryptoListMarketAdapter.CryptoListMarketViewHolder>(),
    Filterable,
    ITHSwipe {

    private var cryptoList: ArrayList<Crypto> = ArrayList()
    private var cryptoListFull: ArrayList<Crypto> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoListMarketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_market_crypto_list, parent, false)
        return CryptoListMarketViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoListMarketViewHolder, position: Int) {
        val selectedCrypto = cryptoList[position]
        holder.bind(selectedCrypto)
        holder.binding.parentLayoutMarket.setOnClickListener {
            val bundle = bundleOf("selectedCrypto" to selectedCrypto)
            findNavController(it)
                .navigate(R.id.action_fragmentMarket_to_dialogCryptoDetail, bundle)
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
        val cryptoId = cryptoList[position].id
        val cryptoSymbol = cryptoList[position].symbol?.uppercase()
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
                                                    mAlarmManager.deleteAlarmManager()
                                                }
                                            }
                                        }
                                    }
                                }.show()
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
                    )
                    .show()
            }
        }
    }

    class CryptoListMarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val binding = AdapterMarketCryptoListBinding.bind(itemView)

        fun bind(crypto: Crypto) {
            Picasso.get().load(crypto.image).into(binding.ivAdapterMarketIcon)
            binding.tvAdapterMarketSymbol.text = crypto.symbol!!.uppercase()
            binding.tvAdapterMarketName.text = crypto.name
            val userCurrency = mPrefs.getCurrencySymbol()
            val currentPrice = crypto.current_price.customFormattedPrice(userCurrency)
            binding.tvAdapterMarketPrice.text = currentPrice
            val priceChange = crypto.price_change_percentage_24h.customFormattedPercentage()
            binding.tvAdapterMarketTextPriceChange.text = priceChange
            if (crypto.price_change_percentage_24h >= 0) {
                binding.ivAdapterMarketIconPriceChange.positivePrice()
                binding.tvAdapterMarketTextPriceChange.positivePrice()
            } else {
                binding.ivAdapterMarketIconPriceChange.negativePrice()
                binding.tvAdapterMarketTextPriceChange.negativePrice()
            }
        }
    }
}