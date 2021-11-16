package com.kotlin.cryptofication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.model.CryptoAlert
import com.kotlin.cryptofication.data.repos.CryptoProvider
import com.kotlin.cryptofication.databinding.AdapterAlertCryptoListBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mRoom
import com.kotlin.cryptofication.utilities.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class CryptoListAlertsAdapter(private val context: Context) :
    RecyclerView.Adapter<CryptoListAlertsAdapter.CryptoListAlertsViewHolder>(),
    Filterable,
    ITHSwipe {

    private var cryptoList: ArrayList<Crypto> = ArrayList()
    private var cryptoListFull: ArrayList<Crypto> = ArrayList()

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
            it.findNavController()
                .navigate(R.id.action_fragmentAlerts_to_dialogCryptoDetail, bundle)
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
        val cryptoSymbol = crypto.id
        Log.d("itemSwipe", "Item position: $position - Item symbol: $cryptoSymbol")

        // Add the item to the database, at the Favorites table (cryptoSymbol and the  current date)
        MainScope().launch {
            val cryptoSwiped = CryptoAlert(cryptoSymbol!!)

            val resultDelete: Int = mRoom.deleteAlert(cryptoSwiped)
            Log.d("itemSwipe", "ResultDelete: $resultDelete")

            when (resultDelete) {
                0 -> {
                    // The item was already in the database
                    Snackbar
                        .make(
                            viewHolder.itemView,
                            "$cryptoSymbol couldn't be removed",
                            Snackbar.LENGTH_LONG
                        )
                        .show()
                }
                else -> {
                    // The item has been added to the database successfully. Add the action to undo the action
                    cryptoList.removeAt(position)
                    notifyItemRemoved(position)
                    CryptoProvider.cryptosAlerts = cryptoList
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
                                            context.showToast("$cryptoSymbol already in favorites")
                                        it > 0 -> {
                                            // The item has been deleted successfully
                                            context.showToast("$cryptoSymbol added to favorites")
                                            Log.d("itemSwipe", "Crypto: $crypto")
                                            cryptoList.add(position, crypto)
                                            notifyItemInserted(position)
                                            CryptoProvider.cryptosAlerts = cryptoList
                                        }
                                    }
                                }
                            }
                        }.show()
                }
            }
        }
    }

    class CryptoListAlertsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val binding = AdapterAlertCryptoListBinding.bind(itemView)

        fun bind(crypto: Crypto) {
            Picasso.get().load(crypto.image).into(binding.ivAdapterAlertIcon)
            binding.tvAdapterAlertSymbol.text = crypto.symbol!!.uppercase()
            binding.tvAdapterAlertName.text = crypto.name
            val userCurrency = mPrefs.getCurrencySymbol()
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


