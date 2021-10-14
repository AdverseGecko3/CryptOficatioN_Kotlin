package com.kotlin.cryptofication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.classes.CryptOficatioNApp
import com.kotlin.cryptofication.classes.DataClass
import com.kotlin.cryptofication.classes.Preferences
import com.kotlin.cryptofication.data.model.CryptoModel
import java.io.Serializable
import com.kotlin.cryptofication.ui.view.DialogCryptoDetail
import java.text.SimpleDateFormat
import java.util.*

class CryptoListAdapter(
    private val context: Context,
    private val cryptoList: ArrayList<CryptoModel>
) :
    RecyclerView.Adapter<CryptoListViewHolder>(), Filterable,
    Serializable, ItemTouchHelperAdapter {
    private var cryptoListFull = ArrayList(cryptoList)
    private var userCurrency: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoListViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_crypto_list, parent, false)
        return CryptoListViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoListViewHolder, position: Int) {
        userCurrency = when (Preferences(CryptOficatioNApp.appContext()).getCurrency()) {
            "eur" -> context.getString(R.string.CURRENCY_EURO)
            "usd" -> context.getString(R.string.CURRENCY_DOLLAR)
            else -> context.getString(R.string.CURRENCY_DOLLAR)
        }
        val selectedCrypto: CryptoModel = cryptoList[position]
        holder.bind(selectedCrypto)
        holder.binding.parentLayout.setOnClickListener {
            val manager = (context as AppCompatActivity).supportFragmentManager
            val alertDialog = DialogCryptoDetail(selectedCrypto, userCurrency!!)
            alertDialog.show(manager, "fragment_alert")
        }
    }

    override fun getItemCount(): Int = cryptoList.size

    override fun getFilter(): Filter {
        return filter
    }

    private val filter: Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filteredList: MutableList<CryptoModel> = ArrayList<CryptoModel>()
            if (charSequence.isEmpty()) {
                filteredList.addAll(cryptoListFull)
            } else {
                val filterPattern = charSequence.toString().lowercase().trim { it <= ' ' }
                for (crypto in cryptoListFull) {
                    if (crypto.name.lowercase().contains(filterPattern)) {
                        filteredList.add(crypto)
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
            cryptoList.addAll(filterResults.values as ArrayList<CryptoModel>)
            notifyDataSetChanged()
        }
    }

    override fun onItemSwiped(direction: Int, viewHolder: RecyclerView.ViewHolder) {
        // Get the position and the crypto symbol of the item
        val position: Int = viewHolder.bindingAdapterPosition
        val cryptoSymbol: String = cryptoList[position].symbol
        Log.d("itemSwipe", "Item position: $position - Item symbol: $cryptoSymbol")

        // Add the item to the database, at the Favorites table (cryptoSymbol and the  current date)
        val resultInsert: Int = DataClass.db.insertToFavorites(
            cryptoSymbol,
            SimpleDateFormat(
                "yyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            )
                .format(System.currentTimeMillis())
        )
        when (resultInsert) {
            -1 -> {
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
            0 ->                     // The item couldn't be added to the database
                Snackbar
                    .make(
                        viewHolder.itemView,
                        "An error occurred while adding $cryptoSymbol to favorites",
                        Snackbar.LENGTH_LONG
                    )
                    .show()
            1 -> {
                // The item has been added to the database successfully. Add the action to undo the action
                notifyItemChanged(position)
                Snackbar
                    .make(
                        viewHolder.itemView,
                        "$cryptoSymbol added to favorites",
                        Snackbar.LENGTH_LONG
                    )
                    .setAction("UNDO") {
                        // When undo is clicked, delete the item from table Favorites
                        when (DataClass.db.deleteFromFavorites(cryptoSymbol)) {
                            0 ->
                                // The item couldn't be deleted
                                Toast.makeText(
                                    context,
                                    "$cryptoSymbol couldn't be removed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            1 ->
                                // The item has been deleted successfully
                                Toast.makeText(
                                    context,
                                    "$cryptoSymbol removed from Favorites successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }
                    }.show()
            }
        }
    }
}