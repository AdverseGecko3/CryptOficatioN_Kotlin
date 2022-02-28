package com.kotlin.cryptofication.ui.view

import android.annotation.SuppressLint
import android.os.Bundle
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
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.adapter.CryptoListMarketAdapter
import com.kotlin.cryptofication.adapter.CryptoListMarketAdapter.OnCryptoClickedListener
import com.kotlin.cryptofication.adapter.CryptoListMarketAdapter.OnSnackbarCreatedListener
import com.kotlin.cryptofication.adapter.CryptoListMarketAdapter.OnLoadMoreClickedListener
import com.kotlin.cryptofication.adapter.SimpleItemTouchHelperCallback
import com.kotlin.cryptofication.adapter.SimpleItemTouchHelperCallback.SelectedChangeListener
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.databinding.FragmentMarketBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.ui.viewmodel.MarketViewModel
import com.kotlin.cryptofication.utilities.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentMarket : Fragment(), SelectedChangeListener,
    OnCryptoClickedListener, OnSnackbarCreatedListener, OnLoadMoreClickedListener {

    @Inject
    lateinit var rwCryptoAdapter: CryptoListMarketAdapter

    private var _binding: FragmentMarketBinding? = null
    private val binding get() = _binding!!
    private val marketViewModel: MarketViewModel by hiltNavGraphViewModels(R.id.my_nav)
    private lateinit var mItemTouchHelper: ItemTouchHelper
    private var itemMarketCap: MenuItem? = null
    private var itemSymbol: MenuItem? = null
    private var itemName: MenuItem? = null
    private var itemPrice: MenuItem? = null
    private var itemPercentage: MenuItem? = null
    private var itemAscending: MenuItem? = null
    private var itemDescending: MenuItem? = null
    private var itemSearch: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketBinding.inflate(inflater, container, false)

        // Enable options menu in fragment
        setHasOptionsMenu(true)

        // Insert custom toolbar
        (activity as AppCompatActivity).supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.toolbar_home)
            elevation = 10f
        }

        //Initialize RecyclerView
        initRecyclerView()

        if (mPrefs.getFirstRun()) {
            // Check if device is running MIUI
            if (marketViewModel.isMiUi()) {
                // Show Xiaomi optimization warning
                val builder = AlertDialog.Builder(
                    activity as AppCompatActivity,
                    R.style.CustomAlertDialog
                )
                val layInflater = (activity as AppCompatActivity).layoutInflater

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
            // See if searchView is expanded
            if (itemSearch!!.isActionViewExpanded) {
                itemSearch!!.collapseActionView()
            }

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

        marketViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("FragmentMarket", "isLoading changed to $isLoading")

            // Set refreshing depending isLoading boolean in ViewModel
            binding.srlMarketReload.isRefreshing = isLoading
        }

        marketViewModel.cryptoLiveData.observe(viewLifecycleOwner) { cryptoList ->
            Log.d("FragmentMarket", "Received cryptoList")

            marketViewModel.cryptoList = ArrayList(cryptoList)

            // Set the cryptoList from API to the adapter
            setListToAdapter()

            // Start ViewFlipper
            initViewFlipper(cryptoList.map { it as Crypto })
        }

        marketViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Log.d("FragmentMarket", "Error message")

            // Show toast when result is empty/null on ViewModel
            requireContext().showToast(errorMessage)

            initViewFlipper(arrayListOf())
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
        itemSearch = menu.findItem(R.id.mnSearch)
        val viewSearch = itemSearch!!.actionView as SearchView
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
        itemSearch!!.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
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
        // Get the ID of the selected item
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
        binding.apply {
            rwMarketCryptoList.layoutManager = LinearLayoutManager(context)
            rwMarketCryptoList.adapter = rwCryptoAdapter
            rwMarketCryptoList.setHasFixedSize(true)
        }

        rwCryptoAdapter.apply {
            setOnCryptoClickedListener(this@FragmentMarket)
            setOnSnackbarCreatedListener(this@FragmentMarket)
            setOnLoadMoreClickedListener(this@FragmentMarket)
        }

        // Attach ItemTouchHelper (swipe items to favorite)
        val callback = SimpleItemTouchHelperCallback(rwCryptoAdapter, this, "market")
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(binding.rwMarketCryptoList)
    }

    override fun onSelectedChange(swipingState: Boolean) {
        binding.srlMarketReload.isEnabled = !swipingState
    }

    override fun onCryptoClicked(bundle: Bundle) {
        try {
            findNavController()
                .navigate(R.id.action_fragmentMarket_to_dialogCryptoDetail, bundle)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            e.message?.let { message -> Log.e("onCryptoClicked", message) }
        }
    }

    override fun onSnackbarCreated(snackbar: Snackbar) {
        snackbar.apply {
            anchorView = activity?.findViewById(R.id.navBottom)
            show()
        }
    }

    override fun onLoadMoreClicked() {
        marketViewModel.onLoadMorePages()
    }

    private fun setListToAdapter() {
        var i = 0
        while (i <= marketViewModel.cryptoList.size) {
            val adView = AdView(requireContext()).apply {
                adSize = AdSize.BANNER
                adUnitId = resources.getString(R.string.ADMOB_BANNER_RECYCLERVIEW)
            }
            marketViewModel.cryptoList.add(i, adView)
            i += Constants.ITEMS_PER_AD
        }
        loadBannerAds()
        marketViewModel.cryptoList.add(Double)
        rwCryptoAdapter.setCryptos(marketViewModel.cryptoList)
    }

    private fun loadBannerAds() {
        loadBannerAd(0)
    }

    private fun loadBannerAd(index: Int) {
        if (index >= marketViewModel.cryptoList.size) {
            return
        }

        val item = marketViewModel.cryptoList[index] as? AdView
            ?: throw ClassCastException("Expected item at index $index to be a banner ad ad.")

        // Set an AdListener on the AdView to wait for the previous banner ad to finish loading before loading the next ad in the items list.
        item.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                // The previous banner ad loaded successfully, call this method again to
                // load the next ad in the items list.
                loadBannerAd(index + Constants.ITEMS_PER_AD)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // The previous banner ad failed to load. Call this method again to load
                // the next ad in the items list.
                val error = String.format(
                    "domain: %s, code: %d, message: %s",
                    loadAdError.domain, loadAdError.code, loadAdError.message
                )
                Log.e(
                    "FragmentMarket",
                    "The previous banner ad failed to load with error: "
                            + error
                            + ". Attempting to"
                            + " load the next banner ad in the items list."
                )
                loadBannerAd(index + Constants.ITEMS_PER_AD)
            }
        }

        // Load the banner ad.
        item.loadAd(AdRequest.Builder().build())
    }

    private fun initViewFlipper(cryptoList: List<Crypto>) {
        Log.d("initVF", "${marketViewModel.hasAlreadyData}")
        if (cryptoList.isEmpty()) {
            if (!marketViewModel.hasAlreadyData) {
                binding.apply {
                    clErrorFragmentMarket.visibility = View.VISIBLE
                    vfLoadedFragmentMarket.visibility = View.GONE
                    tvViewFlipperError.text = resources.getString(R.string.VIEWFLIPPER_EMPTY)
                    vSeparator.below(binding.clErrorFragmentMarket)
                }
            }
            return
        }
        binding.apply {
            vfLoadedFragmentMarket.visibility = View.VISIBLE
            clErrorFragmentMarket.visibility = View.GONE
            vSeparator.below(binding.vfLoadedFragmentMarket)
        }

        val animationIn = AnimationUtils.loadAnimation(mAppContext, R.anim.enter_from_right)
        val animationOut = AnimationUtils.loadAnimation(mAppContext, R.anim.exit_to_left)
        binding.apply {
            vfLoadedFragmentMarket.inAnimation = animationIn
            vfLoadedFragmentMarket.outAnimation = animationOut
            vfLoadedFragmentMarket.startFlipping()
        }

        val cryptoListSorted =
            cryptoList.sortedByDescending { crypto -> crypto.price_change_percentage_24h }
        val firstCrypto = cryptoListSorted[0]
        val secondCrypto = cryptoListSorted[1]
        val thirdCrypto = cryptoListSorted[2]
        val userCurrency = mPrefs.getCurrencySymbol()

        binding.apply {
            tvVFOneLoadedCryptoSymbol.text = firstCrypto.symbol!!.uppercase()
            val currentPrice = firstCrypto.current_price.customFormattedPrice(userCurrency)
            tvVFOneLoadedCryptoCurrentPrice.text = currentPrice
            val priceChange = firstCrypto.price_change_percentage_24h.customFormattedPercentage()
            tvVFOneLoadedCryptoPriceChange.text = priceChange
            if (firstCrypto.price_change_percentage_24h >= 0) {
                ivVFOneLoadedCryptoPriceChange.positivePrice()
                tvVFOneLoadedCryptoCurrentPrice.positivePrice()
                tvVFOneLoadedCryptoPriceChange.positivePrice()
            } else {
                ivVFOneLoadedCryptoPriceChange.negativePrice()
                tvVFOneLoadedCryptoCurrentPrice.negativePrice()
                tvVFOneLoadedCryptoPriceChange.negativePrice()
            }
            clViewFlipperOneLoadedFragmentMarket.setOnClickListener {
                val bundle = bundleOf("selectedCrypto" to firstCrypto)
                findNavController()
                    .navigate(R.id.action_fragmentMarket_to_dialogCryptoDetail, bundle)
            }
        }
        binding.apply {
            tvVFTwoLoadedCryptoSymbol.text = secondCrypto.symbol!!.uppercase()
            val currentPrice = secondCrypto.current_price.customFormattedPrice(userCurrency)
            tvVFTwoLoadedCryptoCurrentPrice.text = currentPrice
            val priceChange = secondCrypto.price_change_percentage_24h.customFormattedPercentage()
            tvVFTwoLoadedCryptoPriceChange.text = priceChange
            if (secondCrypto.price_change_percentage_24h >= 0) {
                ivVFTwoLoadedCryptoPriceChange.positivePrice()
                tvVFTwoLoadedCryptoCurrentPrice.positivePrice()
                tvVFTwoLoadedCryptoPriceChange.positivePrice()
            } else {
                ivVFTwoLoadedCryptoPriceChange.negativePrice()
                tvVFTwoLoadedCryptoCurrentPrice.negativePrice()
                tvVFTwoLoadedCryptoPriceChange.negativePrice()
            }
            clViewFlipperTwoLoadedFragmentMarket.setOnClickListener {
                val bundle = bundleOf("selectedCrypto" to secondCrypto)
                findNavController()
                    .navigate(R.id.action_fragmentMarket_to_dialogCryptoDetail, bundle)
            }
        }

        binding.apply {
            tvVFThreeLoadedCryptoSymbol.text = thirdCrypto.symbol!!.uppercase()
            val currentPrice = thirdCrypto.current_price.customFormattedPrice(userCurrency)
            tvVFThreeLoadedCryptoCurrentPrice.text = currentPrice
            val priceChange = thirdCrypto.price_change_percentage_24h.customFormattedPercentage()
            tvVFThreeLoadedCryptoPriceChange.text = priceChange
            if (thirdCrypto.price_change_percentage_24h >= 0) {
                ivVFThreeLoadedCryptoPriceChange.positivePrice()
                tvVFThreeLoadedCryptoCurrentPrice.positivePrice()
                tvVFThreeLoadedCryptoPriceChange.positivePrice()
            } else {
                ivVFThreeLoadedCryptoPriceChange.negativePrice()
                tvVFThreeLoadedCryptoCurrentPrice.negativePrice()
                tvVFThreeLoadedCryptoPriceChange.negativePrice()
            }
            clViewFlipperThreeLoadedFragmentMarket.setOnClickListener {
                val bundle = bundleOf("selectedCrypto" to thirdCrypto)
                findNavController()
                    .navigate(R.id.action_fragmentMarket_to_dialogCryptoDetail, bundle)
            }
        }
    }

    override fun onResume() {
        for (item in marketViewModel.cryptoList) {
            if (item is AdView) {
                item.resume()
            }
        }
        super.onResume()
    }

    override fun onPause() {
        for (item in marketViewModel.cryptoList) {
            if (item is AdView) {
                item.pause()
            }
        }
        super.onPause()
    }

    override fun onDestroy() {
        _binding = null
        for (item in marketViewModel.cryptoList) {
            if (item is AdView) {
                item.destroy()
            }
        }
        super.onDestroy()
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