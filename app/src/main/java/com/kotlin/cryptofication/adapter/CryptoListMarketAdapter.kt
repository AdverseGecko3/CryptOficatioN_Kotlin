package com.kotlin.cryptofication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.model.CryptoAlert
import com.kotlin.cryptofication.databinding.AdapterAlertCryptoListBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mRoom
import com.kotlin.cryptofication.ui.view.DialogCryptoDetail
import com.kotlin.cryptofication.utilities.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class CryptoListMarketAdapter(private val context: Context) :
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
        val userCurrency = mPrefs.getCurrencySymbol()
        val selectedCrypto = cryptoList[position]
        holder.bind(selectedCrypto)
        holder.binding.parentLayout.setOnClickListener {
            val manager = (context as AppCompatActivity).supportFragmentManager
            val alertDialog = DialogCryptoDetail(selectedCrypto, userCurrency)
            alertDialog.show(manager, "fragment_alert")
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
                    if (crypto.name.lowercase().contains(filterPattern)) {
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
        val cryptoSymbol = cryptoList[position].id
        Log.d("itemSwipe", "Item position: $position - Item symbol: $cryptoSymbol")

        // Add the item to the database, at the Favorites table (cryptoSymbol and the  current date)
        val cryptoSwiped = CryptoAlert(cryptoSymbol)
        MainScope().launch {
            val resultInsert: Int = try {
                mRoom.insertAlert(cryptoSwiped).toInt()
            } catch (e: SQLiteConstraintException) {
                e.printStackTrace()
                0
            }
            Log.d("itemSwipe", "ResultInsert: $resultInsert")

            when (resultInsert) {
                -1 -> {
                    // The item couldn't be added to the database
                    notifyItemChanged(position)
                    Snackbar
                        .make(
                            viewHolder.itemView,
                            "An error occurred while adding $cryptoSymbol to favorites",
                            Snackbar.LENGTH_LONG
                        )
                        .show()
                }
                0 -> {
                    // The item was already in the database
                    notifyItemChanged(position)
                    Snackbar
                        .make(
                            viewHolder.itemView,
                            "$cryptoSymbol already in favorites",
                            Snackbar.LENGTH_LONG
                        )
                        .show()
                }
                else -> {
                    // The item has been added to the database successfully. Add the action to undo the action
                    notifyItemChanged(position)
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
                                        context.showToast("$cryptoSymbol couldn't be removed", 0)
                                    1 ->
                                        // The item has been deleted successfully
                                        context.showToast("$cryptoSymbol removed from Alerts")
                                }
                            }
                        }.show()
                }
            }
        }

    }

    class CryptoListMarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val binding = AdapterAlertCryptoListBinding.bind(itemView)

        fun bind(crypto: Crypto) {
            Picasso.get().load(crypto.image).into(binding.ivAdapterCryptoIcon)
            binding.tvAdapterCryptoSymbol.text = crypto.symbol.uppercase()
            binding.tvAdapterCryptoName.text = crypto.name
            val userCurrency = mPrefs.getCurrencySymbol()
            val currentPrice = crypto.current_price.customFormattedPrice(userCurrency)
            binding.tvAdapterCryptoPrice.text = currentPrice
            val priceChange = crypto.price_change_percentage_24h.customFormattedPercentage()
            binding.tvAdapterCryptoTextPriceChange.text = priceChange
            if (crypto.price_change_percentage_24h >= 0) {
                binding.ivAdapterCryptoIconPriceChange.positivePrice()
                binding.tvAdapterCryptoTextPriceChange.positivePrice()
            } else {
                binding.ivAdapterCryptoIconPriceChange.negativePrice()
                binding.tvAdapterCryptoTextPriceChange.negativePrice()
            }
        }
    }
}