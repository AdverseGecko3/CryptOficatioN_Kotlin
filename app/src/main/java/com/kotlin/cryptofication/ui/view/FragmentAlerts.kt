package com.kotlin.cryptofication.ui.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.adapter.CryptoListAlertsAdapter
import com.kotlin.cryptofication.adapter.CryptoListAlertsAdapter.OnCryptoClickedListener
import com.kotlin.cryptofication.adapter.CryptoListAlertsAdapter.OnCryptoEmptiedListener
import com.kotlin.cryptofication.adapter.CryptoListAlertsAdapter.OnSnackbarCreatedListener
import com.kotlin.cryptofication.adapter.SimpleItemTouchHelperCallback
import com.kotlin.cryptofication.adapter.SimpleItemTouchHelperCallback.SelectedChangeListener
import com.kotlin.cryptofication.databinding.FragmentAlertsBinding
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.ui.viewmodel.AlertsViewModel
import com.kotlin.cryptofication.utilities.Constants
import com.kotlin.cryptofication.utilities.showToast
import java.lang.IllegalArgumentException

class FragmentAlerts : Fragment(), SelectedChangeListener,
    OnCryptoClickedListener, OnCryptoEmptiedListener, OnSnackbarCreatedListener {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private val alertsViewModel: AlertsViewModel by navGraphViewModels(R.id.my_nav)
    private lateinit var rwCryptoAdapter: CryptoListAlertsAdapter
    private lateinit var mItemTouchHelper: ItemTouchHelper
    private var itemMarketCap: MenuItem? = null
    private var itemSymbol: MenuItem? = null
    private var itemName: MenuItem? = null
    private var itemPrice: MenuItem? = null
    private var itemPercentage: MenuItem? = null
    private var itemAscending: MenuItem? = null
    private var itemDescending: MenuItem? = null
    private var itemSearch: MenuItem? = null

    private var cryptoList: ArrayList<Any> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)

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

        // SwipeRefreshLayout refresh listener
        binding.srlAlertsReload.setOnRefreshListener {
            // See if searchView is expanded
            if(itemSearch!!.isActionViewExpanded) {
                itemSearch!!.collapseActionView()
            }

            // See checked filters
            when {
                itemMarketCap!!.isChecked -> alertsViewModel.orderOption = 0
                itemSymbol!!.isChecked -> alertsViewModel.orderOption = 1
                itemName!!.isChecked -> alertsViewModel.orderOption = 2
                itemPrice!!.isChecked -> alertsViewModel.orderOption = 3
                itemPercentage!!.isChecked -> alertsViewModel.orderOption = 4
            }
            when {
                itemAscending!!.isChecked -> alertsViewModel.orderFilter = 0
                itemDescending!!.isChecked -> alertsViewModel.orderFilter = 1
            }

            // Load crypto data from API
            alertsViewModel.onCreate()
        }

        // Swipe refresh customization
        binding.srlAlertsReload.setColorSchemeResources(R.color.purple_app_accent)

        alertsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("FragmentAlerts", "isLoading changed to $isLoading")

            // Set refreshing depending isLoading boolean in ViewModel
            binding.srlAlertsReload.isRefreshing = isLoading
        }

        alertsViewModel.cryptoLiveData.observe(viewLifecycleOwner) { cryptoList ->
            Log.d("FragmentAlerts", "Received cryptoList")

            this.cryptoList = ArrayList(cryptoList)

            // Set the cryptoList from API to the adapter
            setListToAdapter()
        }

        alertsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Log.d("FragmentAlerts", "Error message")

            // Show toast when result is empty/null on ViewModel
            requireContext().showToast(errorMessage)
        }

        // Load crypto data from API now
        binding.srlAlertsReload.post {
            Log.d("FragmentAlerts", "Post SwipeRefresh")
            alertsViewModel.onCreate()
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
            "orderOption:${alertsViewModel.orderOption} - orderFilter:${alertsViewModel.orderFilter}"
        )

        // Check if it is the first run
        if (!alertsViewModel.alreadyLaunched) {
            // Load the starting filters from the SharedPreferences and check the options
            val userFilterOption = mPrefs.getFilterOption()
            val userFilterOrder = mPrefs.getFilterOrder()
            when (userFilterOption.toInt()) {
                0 -> {
                    itemMarketCap!!.isChecked = true
                    alertsViewModel.orderOption = 0
                    alertsViewModel.lastSelectedFilterItem = itemMarketCap!!.itemId
                }
                1 -> {
                    itemSymbol!!.isChecked = true
                    alertsViewModel.orderOption = 1
                    alertsViewModel.lastSelectedFilterItem = itemSymbol!!.itemId
                }
                2 -> {
                    itemName!!.isChecked = true
                    alertsViewModel.orderOption = 2
                    alertsViewModel.lastSelectedFilterItem = itemName!!.itemId
                }
                3 -> {
                    itemPrice!!.isChecked = true
                    alertsViewModel.orderOption = 3
                    alertsViewModel.lastSelectedFilterItem = itemPrice!!.itemId
                }
                4 -> {
                    itemPercentage!!.isChecked = true
                    alertsViewModel.orderOption = 4
                    alertsViewModel.lastSelectedFilterItem = itemPercentage!!.itemId
                }
            }
            when (userFilterOrder.toInt()) {
                0 -> {
                    itemAscending!!.isChecked = true
                    alertsViewModel.orderFilter = 0
                }
                1 -> {
                    itemDescending!!.isChecked = true
                    alertsViewModel.orderFilter = 1
                }
            }

            // Set alreadyLaunched to true in ViewModel
            alertsViewModel.alreadyLaunched = true
            Log.d("firstLaunchFA", "First launch FA")
        } else {
            // See orderOption and orderFilter in ViewModel, and check the corresponding options
            when (alertsViewModel.orderOption) {
                0 -> {
                    itemMarketCap!!.isChecked = true
                    alertsViewModel.lastSelectedFilterItem = itemMarketCap!!.itemId
                }
                1 -> {
                    itemSymbol!!.isChecked = true
                    alertsViewModel.lastSelectedFilterItem = itemSymbol!!.itemId
                }
                2 -> {
                    itemName!!.isChecked = true
                    alertsViewModel.lastSelectedFilterItem = itemName!!.itemId
                }
                3 -> {
                    itemPrice!!.isChecked = true
                    alertsViewModel.lastSelectedFilterItem = itemPrice!!.itemId
                }
                4 -> {
                    itemPercentage!!.isChecked = true
                    alertsViewModel.lastSelectedFilterItem = itemPercentage!!.itemId
                }
            }
            when (alertsViewModel.orderFilter) {
                0 -> itemAscending!!.isChecked = true
                1 -> itemDescending!!.isChecked = true
            }
        }
        Log.d(
            "onCreateOptionsMenu",
            "orderOption:${alertsViewModel.orderOption} - orderFilter:${alertsViewModel.orderFilter}"
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
                "orderOption:${alertsViewModel.orderOption} - orderFilter:${alertsViewModel.orderFilter}"
            )
            if (itemId == R.id.mnFilterOptionMarketCap || itemId == R.id.mnFilterOptionSymbol ||
                itemId == R.id.mnFilterOptionName || itemId == R.id.mnFilterOptionPrice ||
                itemId == R.id.mnFilterOptionPercentage
            ) {
                if (alertsViewModel.lastSelectedFilterItem == itemId) {
                    if (itemAscending!!.isChecked) {
                        itemDescending!!.isChecked = true
                        alertsViewModel.orderFilter = 1
                    } else {
                        itemAscending!!.isChecked = true
                        alertsViewModel.orderFilter = 0
                    }
                } else {
                    when (itemId) {
                        R.id.mnFilterOptionMarketCap -> {
                            itemMarketCap!!.isChecked = true
                            alertsViewModel.orderOption = 0
                        }
                        R.id.mnFilterOptionSymbol -> {
                            itemSymbol!!.isChecked = true
                            alertsViewModel.orderOption = 1
                        }
                        R.id.mnFilterOptionName -> {
                            itemName!!.isChecked = true
                            alertsViewModel.orderOption = 2
                        }
                        R.id.mnFilterOptionPrice -> {
                            itemPrice!!.isChecked = true
                            alertsViewModel.orderOption = 3
                        }
                        R.id.mnFilterOptionPercentage -> {
                            itemPercentage!!.isChecked = true
                            alertsViewModel.orderOption = 4
                        }
                    }
                    itemAscending!!.isChecked = true
                    alertsViewModel.orderFilter = 0
                    alertsViewModel.lastSelectedFilterItem = itemId
                }
            } else {
                alertsViewModel.orderOption = when {
                    itemMarketCap!!.isChecked -> 0
                    itemSymbol!!.isChecked -> 1
                    itemName!!.isChecked -> 2
                    itemPrice!!.isChecked -> 3
                    else -> 4
                }
                when (itemId) {
                    R.id.mnFilterOrderAscending -> {
                        itemAscending!!.isChecked = true
                        alertsViewModel.orderFilter = 0
                    }
                    R.id.mnFilterOrderDescending -> {
                        itemDescending!!.isChecked = true
                        alertsViewModel.orderFilter = 1
                    }
                }
            }
            Log.d(
                "onOptionsItemSelected",
                "orderOption:${alertsViewModel.orderOption} - orderFilter:${alertsViewModel.orderFilter}"
            )
            alertsViewModel.onFilterChanged()
        }
        return true
    }

    private fun initRecyclerView() {
        // Initialize RecyclerView layout manager and adapter
        rwCryptoAdapter = CryptoListAlertsAdapter()
        binding.apply {
            rwAlertsCryptoList.layoutManager = LinearLayoutManager(context)
            rwAlertsCryptoList.adapter = rwCryptoAdapter
            rwAlertsCryptoList.setHasFixedSize(true)
        }
        rwCryptoAdapter.apply {
            setOnCryptoClickedListener(this@FragmentAlerts)
            setOnCryptoEmptiedListener(this@FragmentAlerts)
            setOnSnackbarCreatedListener(this@FragmentAlerts)
        }

        // Attach ItemTouchHelper (swipe items to favorite)
        val callback = SimpleItemTouchHelperCallback(rwCryptoAdapter, this, "alerts")
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(binding.rwAlertsCryptoList)
    }

    override fun onSelectedChange(swipingState: Boolean) {
        binding.srlAlertsReload.isEnabled = !swipingState
    }

    override fun onCryptoClicked(bundle: Bundle) {
        try {
            findNavController()
                .navigate(R.id.action_fragmentAlerts_to_dialogCryptoDetail, bundle)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            e.message?.let { message -> Log.e("onCryptoClicked", message) }
        }
    }

    override fun onCryptoEmptied(isEmpty: Boolean) {
        changeItemsVisibility(isEmpty)
    }

    override fun onSnackbarCreated(snackbar: Snackbar) {
        snackbar.apply {
            anchorView = activity?.findViewById(R.id.navBottom)
            show()
        }
    }

    private fun setListToAdapter() {
        if (cryptoList.isNotEmpty()) {
            changeItemsVisibility(false)

            var i = 0
            while (i <= cryptoList.size) {
                val adView = AdView(requireContext()).apply {
                    adSize = AdSize.BANNER
                    adUnitId = resources.getString(R.string.ADMOB_BANNER_RECYCLERVIEW)
                }
                cryptoList.add(i, adView)
                i += Constants.ITEMS_PER_AD
            }
            loadBannerAds()
            rwCryptoAdapter.setCryptos(cryptoList)

            if (arguments?.getString("cryptoId") != null) {
                Log.d("CryptoNotification", "Had cryptoId argument")
                rwCryptoAdapter.goToCrypto(arguments?.getString("cryptoId")!!)
                arguments?.remove("cryptoId")
            }
        } else {
            changeItemsVisibility(true)
        }
    }

    private fun loadBannerAds() {
        loadBannerAd(0)
    }

    private fun loadBannerAd(index: Int) {
        if (index >= cryptoList.size) {
            return
        }

        val item = cryptoList[index] as? AdView
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

    private fun changeItemsVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rwAlertsCryptoList.visibility = View.GONE
            binding.tvAlertsCryptoListEmpty.visibility = View.VISIBLE
        } else {
            binding.rwAlertsCryptoList.visibility = View.VISIBLE
            binding.tvAlertsCryptoListEmpty.visibility = View.GONE
        }
    }

    override fun onResume() {
        for (item in cryptoList) {
            if (item is AdView) {
                item.resume()
            }
        }
        super.onResume()
    }

    override fun onPause() {
        for (item in cryptoList) {
            if (item is AdView) {
                item.pause()
            }
        }
        super.onPause()
    }

    override fun onDestroy() {
        _binding = null
        for (item in cryptoList) {
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