package com.kotlin.cryptofication.ui.view

import android.annotation.SuppressLint
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.adapter.CryptoListMarketAdapter
import com.kotlin.cryptofication.databinding.FragmentMarketBinding
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.ui.viewmodel.MarketViewModel
import com.kotlin.cryptofication.adapter.SimpleItemTouchHelperCallback
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.utilities.*
import com.kotlin.cryptofication.adapter.SimpleItemTouchHelperCallback.SelectedChangeListener
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext

class FragmentMarket : Fragment(), SelectedChangeListener {

    private var _binding: FragmentMarketBinding? = null
    private val binding get() = _binding!!
    private val marketViewModel: MarketViewModel by navGraphViewModels(R.id.my_nav)
    private lateinit var rwCryptoAdapter: CryptoListMarketAdapter
    private lateinit var mItemTouchHelper: ItemTouchHelper
    private var itemMarketCap: MenuItem? = null
    private var itemSymbol: MenuItem? = null
    private var itemName: MenuItem? = null
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

        // Enable options menu in fragment
        setHasOptionsMenu(true)

        // Insert custom toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.displayOptions =
            ActionBar.DISPLAY_SHOW_CUSTOM
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowCustomEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setCustomView(R.layout.toolbar_home)
        (requireActivity() as AppCompatActivity).supportActionBar?.elevation = 10f

        //Initialize RecyclerView
        initRecyclerView()

        if (mPrefs.getFirstRun()) {
            // Check if device is running MIUI
            if (marketViewModel.isMiUi()) {
                // Show Xiaomi optimization warning
                val builder = AlertDialog.Builder(
                    requireActivity(),
                    R.style.CustomAlertDialog
                )
                val layInflater = requireActivity().layoutInflater

                @SuppressLint("InflateParams")
                val dialogView = layInflater.inflate(R.layout.dialog_xiaomi_check, null)
                builder.setView(dialogView)
                builder.setNeutralButton(
                    getString(R.string.CLOSE)
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .create()

                val dialog = builder.show()
                dialog.setCustomButtonStyle()

                // Show the dialog
                dialog.show()
            }

            // Set first run (onCreateView) to false
            mPrefs.setFirstRun(false)
            Log.d("firstRun", "App first run")
        }

        // SwipeRefreshLayout refresh listener
        binding.srlMarketReload.setOnRefreshListener {
            // See checked filters
            when {
                itemMarketCap!!.isChecked -> marketViewModel.orderOption = 0
                itemSymbol!!.isChecked -> marketViewModel.orderOption = 1
                itemName!!.isChecked -> marketViewModel.orderOption = 2
                itemPrice!!.isChecked -> marketViewModel.orderOption = 3
                itemPercentage!!.isChecked -> marketViewModel.orderOption = 4
            }
            when {
                itemAscending!!.isChecked -> marketViewModel.orderFilter = 0
                itemDescending!!.isChecked -> marketViewModel.orderFilter = 1
            }

            // Load crypto data from API
            marketViewModel.onCreate()
        }

        // Swipe refresh customization
        binding.srlMarketReload.setColorSchemeResources(R.color.purple_app_accent)

        marketViewModel.isLoading.observe(requireActivity()) { isLoading ->
            Log.d("FragmentMarket", "isLoading changed to $isLoading")

            // Set refreshing depending isLoading boolean in ViewModel
            binding.srlMarketReload.isRefreshing = isLoading
        }

        marketViewModel.cryptoLiveData.observe(requireActivity()) { cryptoList ->
            Log.d("FragmentMarket", "Received cryptoList")

            // Set the cryptoList from API to the adapter
            setListToAdapter(cryptoList)

            // Start ViewFlipper
            initViewFlipper(cryptoList)
        }

        marketViewModel.error.observe(requireActivity()) { errorMessage ->
            Log.d("FragmentMarket", "Error message")

            // Show toast when result is empty/null on ViewModel
            mAppContext.showToast(errorMessage)
        }

        // Load crypto data from API now
        binding.srlMarketReload.post {
            Log.d("FragmentMarket", "Post SwipeRefresh")
            marketViewModel.onCreate()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu and find items on it
        inflater.inflate(R.menu.menu_market_alerts, menu)
        referencesOptionsMenu(menu)

        // Find itemSearch and viewSearch in the toolbar
        val itemSearch = menu.findItem(R.id.mnSearch)
        val viewSearch = itemSearch.actionView as SearchView
        Log.d(
            "onCreateOptionsMenu",
            "orderOption:${marketViewModel.orderOption} - orderFilter:${marketViewModel.orderFilter}"
        )

        // Check if it is the first run
        if (!marketViewModel.alreadyLaunched) {
            // Load the starting filters from the SharedPreferences and check the options
            val userFilterOption = mPrefs.getFilterOption()
            val userFilterOrder = mPrefs.getFilterOrder()
            when (userFilterOption.toInt()) {
                0 -> {
                    itemMarketCap!!.isChecked = true
                    marketViewModel.orderOption = 0
                    marketViewModel.lastSelectedFilterItem = itemMarketCap!!.itemId
                }
                1 -> {
                    itemSymbol!!.isChecked = true
                    marketViewModel.orderOption = 1
                    marketViewModel.lastSelectedFilterItem = itemSymbol!!.itemId
                }
                2 -> {
                    itemName!!.isChecked = true
                    marketViewModel.orderOption = 2
                    marketViewModel.lastSelectedFilterItem = itemName!!.itemId
                }
                3 -> {
                    itemPrice!!.isChecked = true
                    marketViewModel.orderOption = 3
                    marketViewModel.lastSelectedFilterItem = itemPrice!!.itemId
                }
                4 -> {
                    itemPercentage!!.isChecked = true
                    marketViewModel.orderOption = 4
                    marketViewModel.lastSelectedFilterItem = itemPercentage!!.itemId
                }
            }
            when (userFilterOrder.toInt()) {
                0 -> {
                    itemAscending!!.isChecked = true
                    marketViewModel.orderFilter = 0
                }
                1 -> {
                    itemDescending!!.isChecked = true
                    marketViewModel.orderFilter = 1
                }
            }

            // Set alreadyLaunched to true in ViewModel
            marketViewModel.alreadyLaunched = true
            Log.d("firstLaunchFM", "First launch FM")
        } else {
            // See orderOption and orderFilter in ViewModel, and check the corresponding options
            when (marketViewModel.orderOption) {
                0 -> {
                    itemMarketCap!!.isChecked = true
                    marketViewModel.lastSelectedFilterItem = itemMarketCap!!.itemId
                }
                1 -> {
                    itemSymbol!!.isChecked = true
                    marketViewModel.lastSelectedFilterItem = itemSymbol!!.itemId
                }
                2 -> {
                    itemName!!.isChecked = true
                    marketViewModel.lastSelectedFilterItem = itemName!!.itemId
                }
                3 -> {
                    itemPrice!!.isChecked = true
                    marketViewModel.lastSelectedFilterItem = itemPrice!!.itemId
                }
                4 -> {
                    itemPercentage!!.isChecked = true
                    marketViewModel.lastSelectedFilterItem = itemPercentage!!.itemId
                }
            }
            when (marketViewModel.orderFilter) {
                0 -> itemAscending!!.isChecked = true
                1 -> itemDescending!!.isChecked = true
            }
        }
        Log.d(
            "onCreateOptionsMenu",
            "orderOption:${marketViewModel.orderOption} - orderFilter:${marketViewModel.orderFilter}"
        )

        // SearchView and itemSearch listeners
        viewSearch.imeOptions = EditorInfo.IME_ACTION_DONE
        viewSearch.isIconified = false
        viewSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // Apply the query as the user makes some change on the filter (writes something)
                Log.d("QueryTextChanged", query)
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
        if (itemId == R.id.mnFilterOptionMarketCap || itemId == R.id.mnFilterOptionSymbol ||
            itemId == R.id.mnFilterOptionName || itemId == R.id.mnFilterOptionPrice ||
            itemId == R.id.mnFilterOptionPercentage || itemId == R.id.mnFilterOrderAscending ||
            itemId == R.id.mnFilterOrderDescending
        ) {
            Log.d(
                "onOptionsItemSelected",
                "orderOption:${marketViewModel.orderOption} - orderFilter:${marketViewModel.orderFilter}"
            )
            if (itemId == R.id.mnFilterOptionMarketCap || itemId == R.id.mnFilterOptionSymbol ||
                itemId == R.id.mnFilterOptionName || itemId == R.id.mnFilterOptionPrice ||
                itemId == R.id.mnFilterOptionPercentage
            ) {
                if (marketViewModel.lastSelectedFilterItem == itemId) {
                    if (itemAscending!!.isChecked) {
                        itemDescending!!.isChecked = true
                        marketViewModel.orderFilter = 1
                    } else {
                        itemAscending!!.isChecked = true
                        marketViewModel.orderFilter = 0
                    }
                } else {
                    when (itemId) {
                        R.id.mnFilterOptionMarketCap -> {
                            itemMarketCap!!.isChecked = true
                            marketViewModel.orderOption = 0
                        }
                        R.id.mnFilterOptionSymbol -> {
                            itemSymbol!!.isChecked = true
                            marketViewModel.orderOption = 1
                        }
                        R.id.mnFilterOptionName -> {
                            itemName!!.isChecked = true
                            marketViewModel.orderOption = 2
                        }
                        R.id.mnFilterOptionPrice -> {
                            itemPrice!!.isChecked = true
                            marketViewModel.orderOption = 3
                        }
                        R.id.mnFilterOptionPercentage -> {
                            itemPercentage!!.isChecked = true
                            marketViewModel.orderOption = 4
                        }
                    }
                    itemAscending!!.isChecked = true
                    marketViewModel.orderFilter = 0
                    marketViewModel.lastSelectedFilterItem = itemId
                }
            } else {
                marketViewModel.orderOption = when {
                    itemMarketCap!!.isChecked -> 0
                    itemSymbol!!.isChecked -> 1
                    itemName!!.isChecked -> 2
                    itemPrice!!.isChecked -> 3
                    else -> 4
                }
                when (itemId) {
                    R.id.mnFilterOrderAscending -> {
                        itemAscending!!.isChecked = true
                        marketViewModel.orderFilter = 0
                    }
                    R.id.mnFilterOrderDescending -> {
                        itemDescending!!.isChecked = true
                        marketViewModel.orderFilter = 1
                    }
                }
            }
            Log.d(
                "onOptionsItemSelected",
                "orderOption:${marketViewModel.orderOption} - orderFilter:${marketViewModel.orderFilter}"
            )
            marketViewModel.onFilterChanged()
        }
        return true
    }

    private fun initRecyclerView() {
        // Initialize RecyclerView layout manager and adapter
        rwCryptoAdapter = CryptoListMarketAdapter()
        binding.rwMarketCryptoList.layoutManager = LinearLayoutManager(context)
        binding.rwMarketCryptoList.adapter = rwCryptoAdapter
        binding.rwMarketCryptoList.setHasFixedSize(true)

        // Attach ItemTouchHelper (swipe items to favorite)
        val callback = SimpleItemTouchHelperCallback(rwCryptoAdapter, this, "market")
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(binding.rwMarketCryptoList)
    }

    override fun onSelectedChange(swipingState: Boolean) {
        binding.srlMarketReload.isEnabled = !swipingState
    }

    private fun setListToAdapter(cryptoList: List<Crypto>) {
        rwCryptoAdapter.setCryptos(cryptoList)
    }

    private fun initViewFlipper(cryptoList: List<Crypto>) {
        val animationIn = AnimationUtils.loadAnimation(mAppContext, R.anim.enter_from_right)
        val animationOut = AnimationUtils.loadAnimation(mAppContext, R.anim.exit_to_left)
        binding.vfFragmentMarket.inAnimation = animationIn
        binding.vfFragmentMarket.outAnimation = animationOut

        val cryptoListSorted =
            cryptoList.sortedByDescending { crypto -> crypto.price_change_percentage_24h }
        val firstCrypto = cryptoListSorted[0]
        val secondCrypto = cryptoListSorted[1]
        val thirdCrypto = cryptoListSorted[2]
        val userCurrency = mPrefs.getCurrencySymbol()

        binding.tvVFOneCryptoSymbol.text = firstCrypto.symbol!!.uppercase()
        var currentPrice = firstCrypto.current_price.customFormattedPrice(userCurrency)
        binding.tvVFOneCryptoCurrentPrice.text = currentPrice
        var priceChange = firstCrypto.price_change_percentage_24h.customFormattedPercentage()
        binding.tvVFOneCryptoPriceChange.text = priceChange
        if (firstCrypto.price_change_percentage_24h >= 0) {
            binding.ivVFOneCryptoPriceChange.positivePrice()
            binding.tvVFOneCryptoCurrentPrice.positivePrice()
            binding.tvVFOneCryptoPriceChange.positivePrice()
        } else {
            binding.ivVFOneCryptoPriceChange.negativePrice()
            binding.tvVFOneCryptoCurrentPrice.negativePrice()
            binding.tvVFOneCryptoPriceChange.negativePrice()
        }
        binding.clViewFlipperOneFragmentMarket.setOnClickListener {
            val bundle = bundleOf("selectedCrypto" to firstCrypto)
            findNavController().navigate(R.id.action_fragmentMarket_to_dialogCryptoDetail, bundle)
        }

        binding.tvVFTwoCryptoSymbol.text = secondCrypto.symbol!!.uppercase()
        currentPrice = secondCrypto.current_price.customFormattedPrice(userCurrency)
        binding.tvVFTwoCryptoCurrentPrice.text = currentPrice
        priceChange = secondCrypto.price_change_percentage_24h.customFormattedPercentage()
        binding.tvVFTwoCryptoPriceChange.text = priceChange
        if (secondCrypto.price_change_percentage_24h >= 0) {
            binding.ivVFTwoCryptoPriceChange.positivePrice()
            binding.tvVFTwoCryptoCurrentPrice.positivePrice()
            binding.tvVFTwoCryptoPriceChange.positivePrice()
        } else {
            binding.ivVFTwoCryptoPriceChange.negativePrice()
            binding.tvVFTwoCryptoCurrentPrice.negativePrice()
            binding.tvVFTwoCryptoPriceChange.negativePrice()
        }
        binding.clViewFlipperTwoFragmentMarket.setOnClickListener {
            val bundle = bundleOf("selectedCrypto" to secondCrypto)
            findNavController().navigate(R.id.action_fragmentMarket_to_dialogCryptoDetail, bundle)
        }

        binding.tvVFThreeCryptoSymbol.text = thirdCrypto.symbol!!.uppercase()
        currentPrice = thirdCrypto.current_price.customFormattedPrice(userCurrency)
        binding.tvVFThreeCryptoCurrentPrice.text = currentPrice
        priceChange = thirdCrypto.price_change_percentage_24h.customFormattedPercentage()
        binding.tvVFThreeCryptoPriceChange.text = priceChange
        if (thirdCrypto.price_change_percentage_24h >= 0) {
            binding.ivVFThreeCryptoPriceChange.positivePrice()
            binding.tvVFThreeCryptoCurrentPrice.positivePrice()
            binding.tvVFThreeCryptoPriceChange.positivePrice()
        } else {
            binding.ivVFThreeCryptoPriceChange.negativePrice()
            binding.tvVFThreeCryptoCurrentPrice.negativePrice()
            binding.tvVFThreeCryptoPriceChange.negativePrice()
        }
        binding.clViewFlipperThreeFragmentMarket.setOnClickListener {
            val bundle = bundleOf("selectedCrypto" to thirdCrypto)
            findNavController().navigate(R.id.action_fragmentMarket_to_dialogCryptoDetail, bundle)
        }
    }

    private fun referencesOptionsMenu(menu: Menu) {
        itemMarketCap = menu.findItem(R.id.mnFilterOptionMarketCap)
        itemSymbol = menu.findItem(R.id.mnFilterOptionSymbol)
        itemName = menu.findItem(R.id.mnFilterOptionName)
        itemPrice = menu.findItem(R.id.mnFilterOptionPrice)
        itemPercentage = menu.findItem(R.id.mnFilterOptionPercentage)
        itemAscending = menu.findItem(R.id.mnFilterOrderAscending)
        itemDescending = menu.findItem(R.id.mnFilterOrderDescending)
    }
}