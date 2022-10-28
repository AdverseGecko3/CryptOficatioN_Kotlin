package com.adversegecko3.cryptofication.ui.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.SearchView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.adapter.CryptoListAlertsAdapter
import com.adversegecko3.cryptofication.adapter.CryptoListAlertsAdapter.OnCryptoListAlertsListener
import com.adversegecko3.cryptofication.adapter.CryptoListAlertsPortfolioAdapter
import com.adversegecko3.cryptofication.adapter.CryptoListAlertsPortfolioAdapter.OnCryptoListAlertsPortfolioListener
import com.adversegecko3.cryptofication.adapter.SimpleItemTouchHelperCallback
import com.adversegecko3.cryptofication.adapter.SimpleItemTouchHelperCallback.SelectedChangeListener
import com.adversegecko3.cryptofication.data.model.CryptoAlert
import com.adversegecko3.cryptofication.databinding.FragmentAlertsBinding
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.adversegecko3.cryptofication.ui.viewmodel.AlertsViewModel
import com.adversegecko3.cryptofication.utilities.customFormattedPrice
import com.adversegecko3.cryptofication.utilities.showToast
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentAlerts :
    Fragment(), MenuProvider, SelectedChangeListener, OnCryptoListAlertsListener,
    OnCryptoListAlertsPortfolioListener {

    @Inject
    lateinit var rwCryptoAdapter: CryptoListAlertsAdapter

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private val alertsViewModel: AlertsViewModel by hiltNavGraphViewModels(R.id.my_nav)
    private lateinit var rwCryptoPortfolioAdapter: CryptoListAlertsPortfolioAdapter
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
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)

        // Enable options menu in fragment
        val menuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Insert custom toolbar
        (activity as AppCompatActivity).supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.toolbar_home)
            elevation = 10f
        }

        // FrameLayout height and ImageView padding
        layoutParams()

        // BottomSheet behavior
        bottomSheetBehavior()

        //Initialize RecyclerView
        initRecyclerView()

        // SwipeRefreshLayout refresh listener
        binding.srlAlertsReload.setOnRefreshListener {
            // See if searchView is expanded
            if (itemSearch!!.isActionViewExpanded) {
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
            // Set refreshing depending isLoading boolean in ViewModel
            binding.srlAlertsReload.isRefreshing = isLoading
        }

        alertsViewModel.cryptoLiveData.observe(viewLifecycleOwner) { cryptoList ->
            alertsViewModel.cryptoList = ArrayList(cryptoList)

            // Set the cryptoList from API to the adapter
            setCryptoListAlertsToAdapter()
        }

        alertsViewModel.alertsLiveData.observe(viewLifecycleOwner) { alertsList ->
            // Set the cryptoList from API to the adapter
            setCryptoListAlertsPortfolioToAdapter(ArrayList(alertsList))
        }

        alertsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            // Show toast when result is empty/null on ViewModel
            requireContext().showToast(errorMessage)
        }

        // Load crypto data from API now
        binding.srlAlertsReload.post {
            alertsViewModel.onCreate()
        }

        return binding.root
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        // Inflate menu and find items on it
        menuInflater.inflate(R.menu.menu_market_alerts, menu)
        referencesOptionsMenu(menu)

        // Find itemSearch and viewSearch in the toolbar
        itemSearch = menu.findItem(R.id.mnSearch)
        val viewSearch = itemSearch!!.actionView as SearchView
        viewSearch.apply {
            queryHint = "Search coins"
        }

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

        // SearchView and itemSearch listeners
        viewSearch.apply {
            imeOptions = EditorInfo.IME_ACTION_DONE
            isIconified = false
            queryHint = "Filter alerts coins"
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {
                    // Apply the query as the user makes some change on the filter (writes something)
                    rwCryptoAdapter.filter.filter(query)
                    return false
                }
            })
        }
        itemSearch!!.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Open the search
                if (viewSearch.query.isEmpty()) {
                    viewSearch.onActionViewExpanded()
                    alertsViewModel.isSearchOpen = true
                } else {
                    viewSearch.clearFocus()
                }
                return true // True to be able to open
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Close the search, empty the field and clear the focus
                rwCryptoAdapter.filter.filter("")
                alertsViewModel.isSearchOpen = false
                viewSearch.apply {
                    onActionViewCollapsed()
                    setQuery("", false)
                    clearFocus()
                }
                return true // True as we want to be able to close it
            }
        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        // Get the ID of the selected item
        val itemId = menuItem.itemId
        if (itemId == R.id.mnFilterOptionMarketCap || itemId == R.id.mnFilterOptionSymbol ||
            itemId == R.id.mnFilterOptionName || itemId == R.id.mnFilterOptionPrice ||
            itemId == R.id.mnFilterOptionPercentage || itemId == R.id.mnFilterOrderAscending ||
            itemId == R.id.mnFilterOrderDescending
        ) {
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
            alertsViewModel.onFilterChanged()
        }
        return true
    }

    private fun layoutParams() {
        // FrameLayout height
        val fl: FrameLayout = binding.bsAlertsPortfolio
        val params = fl.layoutParams
        params.height = resources.displayMetrics.heightPixels / 2
        fl.layoutParams = params

        // ImageView padding
        val drawableHeight = binding.ivAlertsScrollBottomSheet.drawable.intrinsicHeight / 2
        binding.ivAlertsScrollBottomSheet.setPadding(0, drawableHeight, 0, drawableHeight)
    }

    private fun bottomSheetBehavior() {
        BottomSheetBehavior.from(binding.bsAlertsPortfolio).apply {
            peekHeight = binding.ivAlertsScrollBottomSheet.drawable.intrinsicHeight * 2
            this.state = BottomSheetBehavior.STATE_COLLAPSED
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            binding.ivAlertsScrollBottomSheet.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_arrow_up,
                                    null
                                )
                            )
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            binding.ivAlertsScrollBottomSheet.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_arrow_down,
                                    null
                                )
                            )
                        }
                        BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                            binding.ivAlertsScrollBottomSheet.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_remove,
                                    null
                                )
                            )
                        }
                        else -> {

                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })
        }
        binding.ivAlertsScrollBottomSheet.setOnClickListener {
            when (BottomSheetBehavior.from(binding.bsAlertsPortfolio).state) {
                BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.from(binding.bsAlertsPortfolio).state =
                    BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.from(binding.bsAlertsPortfolio).state =
                    BottomSheetBehavior.STATE_COLLAPSED
                else -> {

                }
            }
        }
    }

    private fun initRecyclerView() {
        // Initialize RecyclerViews layout managers, adapters and interfaces
        rwCryptoPortfolioAdapter = CryptoListAlertsPortfolioAdapter()
        binding.apply {
            rwAlertsCryptoCryptoList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = rwCryptoAdapter
                setHasFixedSize(true)
            }

            rwAlertsCryptoPortfolioList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = rwCryptoPortfolioAdapter
                setHasFixedSize(true)
            }
        }

        rwCryptoAdapter.setOnCryptoListAlertsListener(this)
        rwCryptoPortfolioAdapter.setOnCryptoListAlertsPortfolioListener(this)

        // Attach ItemTouchHelper (swipe items to favorite)
        val callback = SimpleItemTouchHelperCallback(rwCryptoAdapter, this, "alerts")
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(binding.rwAlertsCryptoCryptoList)
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

    override fun onQuantityUpdatedCrypto(cryptoAlert: CryptoAlert) {
        alertsViewModel.updateCryptoAlert(cryptoAlert)
    }

    override fun onQuantityUpdatedTotal(total: Double, bitcoinPrice: Double) {
        binding.tvAlertsCryptoPortfolioTotalText.text = totalText(total, bitcoinPrice)
    }

    private fun totalText(total: Double, bitcoinPrice: Double): String {
        val priceInBitcoin =
            (total / bitcoinPrice).customFormattedPrice(mPrefs.getCurrencySymbol()).dropLast(1)
        return "${
            total.customFormattedPrice(
                mPrefs.getCurrencySymbol(),
                true
            )
        } - $priceInBitcoin BTC"
    }

    override fun onAlertChanged() {
        alertsViewModel.onAlertsUpdated()
    }

    private fun setCryptoListAlertsToAdapter() {
        if (alertsViewModel.cryptoList.isNotEmpty()) {
            changeItemsVisibility(false)

            alertsViewModel.addBanners()
            rwCryptoAdapter.setCryptos(alertsViewModel.cryptoList)

            if (arguments?.getString("cryptoId") != null) {
                // If arguments has a cryptoId (coming from a notification)
                rwCryptoAdapter.goToCrypto(arguments?.getString("cryptoId")!!)
                arguments?.remove("cryptoId")
            }
        } else {
            changeItemsVisibility(true)
        }
    }

    private fun setCryptoListAlertsPortfolioToAdapter(alertsList: ArrayList<CryptoAlert>) {
        rwCryptoPortfolioAdapter.setCryptos(alertsList)
    }

    private fun changeItemsVisibility(isEmpty: Boolean) {
        activity?.runOnUiThread {
            binding.apply {
                if (isEmpty) {
                    rwAlertsCryptoCryptoList.visibility = View.GONE
                    rwAlertsCryptoPortfolioList.visibility = View.GONE
                    bsAlertsPortfolio.visibility = View.GONE
                    tvAlertsCryptoListEmpty.visibility = View.VISIBLE
                } else {
                    rwAlertsCryptoCryptoList.visibility = View.VISIBLE
                    rwAlertsCryptoPortfolioList.visibility = View.VISIBLE
                    bsAlertsPortfolio.visibility = View.VISIBLE
                    tvAlertsCryptoListEmpty.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        for (item in alertsViewModel.cryptoList) {
            if (item is AdView) {
                item.resume()
            }
        }
        super.onResume()
    }

    override fun onPause() {
        for (item in alertsViewModel.cryptoList) {
            if (item is AdView) {
                item.pause()
            }
        }
        super.onPause()
    }

    override fun onDestroy() {
        _binding = null
        for (item in alertsViewModel.cryptoList) {
            if (item is AdView) {
                item.destroy()
            }
        }
        if (alertsViewModel.isSearchOpen) {
            try {
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
            } catch (e: NullPointerException) {
                Log.e(
                    "NullPointerException",
                    e.message ?: "NullPointerException: InputMethodManager"
                )
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