package com.kotlin.cryptofication.ui.view

import android.annotation.SuppressLint
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.adapter.CryptoListAdapter
import com.kotlin.cryptofication.classes.DataClass
import com.kotlin.cryptofication.databinding.FragmentMarketBinding
import com.kotlin.cryptofication.data.model.CryptoModel
import com.kotlin.cryptofication.ui.viewmodel.MarketViewModel
import kotlin.collections.ArrayList
import com.kotlin.cryptofication.adapter.SimpleItemTouchHelperCallback
import com.kotlin.cryptofication.classes.CryptOficatioNApp
import com.kotlin.cryptofication.classes.CryptOficatioNApp.Companion.prefs
import java.text.DecimalFormat
import java.text.NumberFormat


class FragmentMarket : Fragment() {

    private var _binding: FragmentMarketBinding? = null
    private val binding get() = _binding!!
    private val marketViewModel: MarketViewModel by viewModels()
    private lateinit var rwCryptoAdapter: CryptoListAdapter
    private lateinit var mItemTouchHelper: ItemTouchHelper
    private var itemName: MenuItem? = null
    private var itemSymbol: MenuItem? = null
    private var itemPrice: MenuItem? = null
    private var itemPercentage: MenuItem? = null
    private var itemAscending: MenuItem? = null
    private var itemDescending: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        // Insert custom toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.displayOptions =
            ActionBar.DISPLAY_SHOW_CUSTOM
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowCustomEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setCustomView(R.layout.toolbar_home)
        (requireActivity() as AppCompatActivity).supportActionBar?.elevation = 10f

        // SwipeRefreshLayout listener and customization
        binding.srlMarketReload.setOnRefreshListener {
            // When refreshing, load crypto data again
            when {
                itemName!!.isChecked -> {
                    marketViewModel.orderOption = 0
                }
                itemSymbol!!.isChecked -> {
                    marketViewModel.orderOption = 1
                }
                itemPrice!!.isChecked -> {
                    marketViewModel.orderOption = 2
                }
                itemPercentage!!.isChecked -> {
                    marketViewModel.orderOption = 3
                }
            }
            when {
                itemAscending!!.isChecked -> {
                    marketViewModel.orderFilter = 0
                }
                itemDescending!!.isChecked -> {
                    marketViewModel.orderFilter = 1
                }
            }
            marketViewModel.onCreate()
        }

        binding.srlMarketReload.setColorSchemeResources(R.color.purple_app_accent)

        marketViewModel.isLoading.observe(requireActivity(), { isLoading ->
            Log.d("FragmentMarket", "isLoading changed to $isLoading")
            binding.srlMarketReload.isRefreshing = isLoading
        })

        marketViewModel.cryptoLiveData.observe(requireActivity(), { cryptoList ->
            Log.d("FragmentMarket", "Received cryptoList")
            initRecyclerView(cryptoList)
            initViewFlipper(cryptoList)
        })

        marketViewModel.error.observe(requireActivity(), { errorMessage ->
            Log.d("FragmentMarket", "Error message")
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        })

        // Fetching data from server
        binding.srlMarketReload.post {
            Log.d("FragmentMarket", "Post SwipeRefresh")
            marketViewModel.onCreate()
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu and find items on it
        inflater.inflate(R.menu.menu, menu)
        referencesOptionsMenu(menu)

        // Find itemSearch and viewSearch in the toolbar
        val itemSearch = menu.findItem(R.id.mnSearch)
        val viewSearch = itemSearch.actionView as SearchView

        // If it is the first run load the starting filters from the SharedPreferences
        if (DataClass.firstRun) {
            Log.d("onCreateOptionsMenu", "First run")
            val userFilterOption = prefs.getFilterOption()
            val userFilterOrder = prefs.getFilterOrder()
            when (userFilterOption.toInt()) {
                0 -> {
                    itemName?.isChecked = true
                    marketViewModel.orderOption = 0

                    // Get the ID of the item selected previously ( of the new one
                    marketViewModel.lastSelectedFilterItem = itemName!!.itemId
                }
                1 -> {
                    itemSymbol?.isChecked = true
                    marketViewModel.orderOption = 1

                    // Get the ID of the item selected previously ( of the new one
                    marketViewModel.lastSelectedFilterItem = itemSymbol!!.itemId
                }
                2 -> {
                    itemPrice?.isChecked = true
                    marketViewModel.orderOption = 2

                    // Get the ID of the item selected previously ( of the new one
                    marketViewModel.lastSelectedFilterItem = itemPrice!!.itemId
                }
                3 -> {
                    itemPercentage?.isChecked = true
                    marketViewModel.orderOption = 3

                    // Get the ID of the item selected previously ( of the new one
                    marketViewModel.lastSelectedFilterItem = itemPercentage!!.itemId
                }
            }
            when (userFilterOrder.toInt()) {
                0 -> {
                    itemAscending?.isChecked = true
                    marketViewModel.orderFilter = 0
                }
                1 -> {
                    itemDescending?.isChecked = true
                    marketViewModel.orderFilter = 1
                }
            }

            // Set first run to false
            DataClass.firstRun = false
        } else {
            when (marketViewModel.orderOption) {
                0 -> itemName?.isChecked = true
                1 -> itemSymbol?.isChecked = true
                2 -> itemPrice?.isChecked = true
                3 -> itemPercentage?.isChecked = true

            }
            when (marketViewModel.orderFilter) {
                0 -> itemAscending?.isChecked = true
                1 -> itemDescending?.isChecked = true
                else -> {
                }
            }
            // Get the ID of the item selected previously ( of the new one
            marketViewModel.lastSelectedFilterItem = itemName!!.itemId

        }

        // SearchView and itemSearch listeners
        viewSearch.imeOptions = EditorInfo.IME_ACTION_DONE
        viewSearch.isIconified = false
        viewSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // Apply the query as the user makes some change on the filter (writes something)
                rwCryptoAdapter.filter.filter(query)
                return false
            }
        })
        itemSearch.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Open the search
                Log.d("itemSearch", "Opened search")
                viewSearch.onActionViewExpanded()
                return true // True to be able to open
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Close the search, empty the field and clear the focus
                Log.d("itemSearch", "Closed search")
                rwCryptoAdapter.filter.filter("")
                viewSearch.onActionViewCollapsed()
                viewSearch.setQuery("", false)
                viewSearch.clearFocus()
                return true // True as we want to be able to close it
            }
        })
    }

    @SuppressLint("NonConstantResourceId", "UseCompatLoadingForDrawables")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //  Get the ID of the selected item
        val itemId = item.itemId
        when (itemId) {
            R.id.mnFilterOptionName -> {
                if (marketViewModel.lastSelectedFilterItem == R.id.mnFilterOptionName) {
                    if (itemAscending!!.isChecked) {
                        itemDescending?.isChecked = true
                        marketViewModel.orderFilter = 1
                    } else {
                        itemAscending?.isChecked = true
                        marketViewModel.orderFilter = 0
                    }
                } else {
                    item.isChecked = true
                    itemAscending?.isChecked = true
                    marketViewModel.orderFilter = 0
                }
                marketViewModel.lastSelectedFilterItem = itemId
                marketViewModel.orderOption = 0
            }
            R.id.mnFilterOptionSymbol -> {
                if (marketViewModel.lastSelectedFilterItem == R.id.mnFilterOptionSymbol) {
                    if (itemAscending!!.isChecked) {
                        itemDescending?.isChecked = true
                        marketViewModel.orderFilter = 1
                    } else {
                        itemAscending?.isChecked = true
                        marketViewModel.orderFilter = 0
                    }
                } else {
                    item.isChecked = true
                    itemAscending?.isChecked = true
                    marketViewModel.orderFilter = 0
                }
                marketViewModel.lastSelectedFilterItem = itemId
                marketViewModel.orderOption = 1
            }
            R.id.mnFilterOptionPrice -> {
                if (marketViewModel.lastSelectedFilterItem == R.id.mnFilterOptionPrice) {
                    if (itemAscending!!.isChecked) {
                        itemDescending?.isChecked = true
                        marketViewModel.orderFilter = 1
                    } else {
                        itemAscending?.isChecked = true
                        marketViewModel.orderFilter = 0
                    }
                } else {
                    item.isChecked = true
                    itemAscending?.isChecked = true
                    marketViewModel.orderFilter = 0
                }
                marketViewModel.lastSelectedFilterItem = itemId
                marketViewModel.orderOption = 2
            }
            R.id.mnFilterOptionPercentage -> {
                if (marketViewModel.lastSelectedFilterItem == R.id.mnFilterOptionPercentage) {
                    if (itemAscending!!.isChecked) {
                        itemDescending?.isChecked = true
                        marketViewModel.orderFilter = 1
                    } else {
                        itemAscending?.isChecked = true
                        marketViewModel.orderFilter = 0
                    }
                } else {
                    item.isChecked = true
                    itemAscending?.isChecked = true
                    marketViewModel.orderFilter = 0
                }
                marketViewModel.lastSelectedFilterItem = itemId
                marketViewModel.orderOption = 3
            }
            R.id.mnFilterOrderAscending -> {
                itemAscending?.isChecked = true
                marketViewModel.orderFilter = 0
                marketViewModel.orderOption = when {
                    itemName!!.isChecked -> {
                        0
                    }
                    itemSymbol!!.isChecked -> {
                        1
                    }
                    itemPrice!!.isChecked -> {
                        2
                    }
                    else -> {
                        3
                    }
                }
            }
            R.id.mnFilterOrderDescending -> {
                itemDescending?.isChecked = true
                marketViewModel.orderFilter = 1
                marketViewModel.orderOption = when {
                    itemName!!.isChecked -> {
                        0
                    }
                    itemSymbol!!.isChecked -> {
                        1
                    }
                    itemPrice!!.isChecked -> {
                        2
                    }
                    else -> {
                        3
                    }
                }
            }
        }
        if (itemId == R.id.mnFilterOptionName || itemId == R.id.mnFilterOptionSymbol ||
            itemId == R.id.mnFilterOptionPrice || itemId == R.id.mnFilterOptionPercentage ||
            itemId == R.id.mnFilterOrderAscending || itemId == R.id.mnFilterOrderDescending
        ) {
            marketViewModel.onFilterChanged()
        }
        return true
    }

    private fun initRecyclerView(cryptoList: List<CryptoModel>) {
        // Initialize RecyclerView manager and adapter
        rwCryptoAdapter = CryptoListAdapter(
            requireActivity(),
            ArrayList(cryptoList)
        )
        binding.rwMarketCryptoList.layoutManager = LinearLayoutManager(context)
        binding.rwMarketCryptoList.adapter = rwCryptoAdapter
        binding.rwMarketCryptoList.setHasFixedSize(true)

        val callback =
            SimpleItemTouchHelperCallback(rwCryptoAdapter, binding.srlMarketReload)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(binding.rwMarketCryptoList)
    }

    private fun initViewFlipper(cryptoList: List<CryptoModel>) {
        // Prepare ViewFlipper
        val animationIn = AnimationUtils.loadAnimation(requireContext(), R.anim.enter_from_right)
        val animationOut = AnimationUtils.loadAnimation(requireContext(), R.anim.exit_to_left)
        binding.vfFragmentMarket.inAnimation = animationIn
        binding.vfFragmentMarket.outAnimation = animationOut

        val cryptoListSorted =
            cryptoList.sortedByDescending { crypto -> crypto.price_change_percentage_24h }
        val firstCrypto = cryptoListSorted[0]
        val secondCrypto = cryptoListSorted[1]
        val thirdCrypto = cryptoListSorted[2]
        val userCurrency = when (prefs.getCurrency()) {
            "eur" -> CryptOficatioNApp.appContext.getString(R.string.CURRENCY_EURO)
            "usd" -> CryptOficatioNApp.appContext.getString(R.string.CURRENCY_DOLLAR)
            else -> CryptOficatioNApp.appContext.getString(R.string.CURRENCY_DOLLAR)
        }
        val nf = NumberFormat.getInstance()
        val currencySeparator = DecimalFormat().decimalFormatSymbols.decimalSeparator

        binding.tvVFOneCryptoSymbol.text = firstCrypto.symbol.uppercase()
        var currentPrice = String.format("%.10f", firstCrypto.current_price)
            .replace("0+$".toRegex(), "")
        if (currentPrice.endsWith(currencySeparator)) {
            currentPrice = currentPrice.substring(0, currentPrice.length - 1)
        }
        binding.tvVFOneCryptoCurrentPrice.text = "$currentPrice$userCurrency"
        var priceChange = String.format("%.2f", firstCrypto.price_change_percentage_24h)
            .replace("0+$".toRegex(), "")
        if (priceChange.endsWith(currencySeparator)) {
            priceChange = priceChange.substring(0, priceChange.length - 1)
        }
        binding.tvVFOneCryptoPriceChange.text = "$priceChange%"
        if (nf.parse(priceChange)!!.toDouble() > 0) {
            binding.ivVFOneCryptoPriceChange.setImageResource(R.drawable.ic_arrow_drop_up)
            binding.ivVFOneCryptoPriceChange.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.green_high)
            )
            binding.tvVFOneCryptoCurrentPrice.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.green_high)
            )
            binding.tvVFOneCryptoPriceChange.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.green_high)
            )
        } else {
            binding.ivVFOneCryptoPriceChange.setImageResource(R.drawable.ic_arrow_drop_down)
            binding.ivVFOneCryptoPriceChange.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.red_low)
            )
            binding.tvVFOneCryptoCurrentPrice.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red_low)
            )
            binding.tvVFOneCryptoPriceChange.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red_low)
            )
        }

        binding.tvVFTwoCryptoSymbol.text = secondCrypto.symbol.uppercase()
        currentPrice = String.format("%.10f", secondCrypto.current_price)
            .replace("0+$".toRegex(), "")
        if (currentPrice.endsWith(currencySeparator)) {
            currentPrice = currentPrice.substring(0, currentPrice.length - 1)
        }
        binding.tvVFTwoCryptoCurrentPrice.text = "$currentPrice$userCurrency"
        priceChange = String.format("%.2f", secondCrypto.price_change_percentage_24h)
            .replace("0+$".toRegex(), "")
        if (priceChange.endsWith(currencySeparator)) {
            priceChange = priceChange.substring(0, priceChange.length - 1)
        }
        binding.tvVFTwoCryptoPriceChange.text = "$priceChange%"
        if (nf.parse(priceChange)!!.toDouble() > 0) {
            binding.ivVFTwoCryptoPriceChange.setImageResource(R.drawable.ic_arrow_drop_up)
            binding.ivVFTwoCryptoPriceChange.setColorFilter(
                ContextCompat.getColor(CryptOficatioNApp.appContext, R.color.green_high)
            )
            binding.tvVFTwoCryptoCurrentPrice.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.green_high)
            )
            binding.tvVFTwoCryptoPriceChange.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.green_high)
            )
        } else {
            binding.ivVFTwoCryptoPriceChange.setImageResource(R.drawable.ic_arrow_drop_down)
            binding.ivVFTwoCryptoPriceChange.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.red_low)
            )
            binding.tvVFTwoCryptoCurrentPrice.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red_low)
            )
            binding.tvVFTwoCryptoPriceChange.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red_low)
            )
        }

        binding.tvVFThreeCryptoSymbol.text = thirdCrypto.symbol.uppercase()
        currentPrice = String.format("%.10f", thirdCrypto.current_price)
            .replace("0+$".toRegex(), "")
        if (currentPrice.endsWith(currencySeparator)) {
            currentPrice = currentPrice.substring(0, currentPrice.length - 1)
        }
        binding.tvVFThreeCryptoCurrentPrice.text = "$currentPrice$userCurrency"
        priceChange = String.format("%.2f", thirdCrypto.price_change_percentage_24h)
            .replace("0+$".toRegex(), "")
        if (priceChange.endsWith(currencySeparator)) {
            priceChange = priceChange.substring(0, priceChange.length - 1)
        }
        binding.tvVFThreeCryptoPriceChange.text = "$priceChange%"
        if (nf.parse(priceChange)!!.toDouble() > 0) {
            binding.ivVFThreeCryptoPriceChange.setImageResource(R.drawable.ic_arrow_drop_up)
            binding.ivVFThreeCryptoPriceChange.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.green_high)
            )
            binding.tvVFThreeCryptoCurrentPrice.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.green_high)
            )
            binding.tvVFThreeCryptoPriceChange.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.green_high)
            )
        } else {
            binding.ivVFThreeCryptoPriceChange.setImageResource(R.drawable.ic_arrow_drop_down)
            binding.ivVFThreeCryptoPriceChange.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.red_low)
            )
            binding.tvVFThreeCryptoCurrentPrice.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red_low)
            )
            binding.tvVFThreeCryptoPriceChange.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red_low)
            )
        }
    }

    private fun referencesOptionsMenu(menu: Menu) {
        itemName = menu.findItem(R.id.mnFilterOptionName)
        itemSymbol = menu.findItem(R.id.mnFilterOptionSymbol)
        itemPrice = menu.findItem(R.id.mnFilterOptionPrice)
        itemPercentage = menu.findItem(R.id.mnFilterOptionPercentage)
        itemAscending = menu.findItem(R.id.mnFilterOrderAscending)
        itemDescending = menu.findItem(R.id.mnFilterOrderDescending)
    }
}