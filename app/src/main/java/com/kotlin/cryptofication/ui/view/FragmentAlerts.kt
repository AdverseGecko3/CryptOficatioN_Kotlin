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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.adapter.CryptoListAlertsAdapter
import com.kotlin.cryptofication.adapter.SimpleItemTouchHelperCallback
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.databinding.FragmentAlertsBinding
import com.kotlin.cryptofication.ui.viewmodel.AlertsViewModel
import com.kotlin.cryptofication.utilities.DataClass
import com.kotlin.cryptofication.utilities.showToast

class FragmentAlerts: Fragment(), SimpleItemTouchHelperCallback.SelectedChangeListener {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private val alertsViewModel: AlertsViewModel by viewModels()
    private lateinit var rwCryptoAdapter: CryptoListAlertsAdapter
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
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)

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

        // SwipeRefreshLayout refresh listener
        binding.srlAlertsReload.setOnRefreshListener {
            // See checked filters
            when {
                itemName!!.isChecked -> {
                    alertsViewModel.orderOption = 0
                }
                itemSymbol!!.isChecked -> {
                    alertsViewModel.orderOption = 1
                }
                itemPrice!!.isChecked -> {
                    alertsViewModel.orderOption = 2
                }
                itemPercentage!!.isChecked -> {
                    alertsViewModel.orderOption = 3
                }
            }
            when {
                itemAscending!!.isChecked -> {
                    alertsViewModel.orderFilter = 0
                }
                itemDescending!!.isChecked -> {
                    alertsViewModel.orderFilter = 1
                }
            }

            // Load crypto data from API
            alertsViewModel.onCreate()
        }

        // Swipe refresh customization
        binding.srlAlertsReload.setColorSchemeResources(R.color.purple_app_accent)

        alertsViewModel.isLoading.observe(requireActivity(), { isLoading ->
            Log.d("FragmentMarket", "isLoading changed to $isLoading")

            // Set refreshing depending isLoading boolean in ViewModel
            binding.srlAlertsReload.isRefreshing = isLoading
        })

        alertsViewModel.cryptoLiveData.observe(requireActivity(), { cryptoList ->
            Log.d("FragmentMarket", "Received cryptoList")

            // Set the cryptoList from API to the adapter
            setListToAdapter(cryptoList)
        })

        alertsViewModel.error.observe(requireActivity(), { errorMessage ->
            Log.d("FragmentMarket", "Error message")

            // Show toast when result is empty/null on ViewModel
            requireContext().showToast(errorMessage)
        })

        // Load crypto data from API now
        binding.srlAlertsReload.post {
            Log.d("FragmentMarket", "Post SwipeRefresh")
            alertsViewModel.onCreate()
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
            "orderOption:${alertsViewModel.orderOption} - orderFilter:${alertsViewModel.orderFilter}"
        )

        // Check if it is the first run
        if (DataClass.firstRun) {
            Log.d("onCreateOptionsMenu", "First run")
            // Load the starting filters from the SharedPreferences and check the options
            val userFilterOption = CryptOficatioNApp.mPrefs.getFilterOption()
            val userFilterOrder = CryptOficatioNApp.mPrefs.getFilterOrder()
            when (userFilterOption.toInt()) {
                0 -> {
                    itemName?.isChecked = true
                    alertsViewModel.orderOption = 0

                    alertsViewModel.lastSelectedFilterItem = itemName!!.itemId
                }
                1 -> {
                    itemSymbol?.isChecked = true
                    alertsViewModel.orderOption = 1

                    alertsViewModel.lastSelectedFilterItem = itemSymbol!!.itemId
                }
                2 -> {
                    itemPrice?.isChecked = true
                    alertsViewModel.orderOption = 2

                    alertsViewModel.lastSelectedFilterItem = itemPrice!!.itemId
                }
                3 -> {
                    itemPercentage?.isChecked = true
                    alertsViewModel.orderOption = 3

                    alertsViewModel.lastSelectedFilterItem = itemPercentage!!.itemId
                }
            }
            when (userFilterOrder.toInt()) {
                0 -> {
                    itemAscending?.isChecked = true
                    alertsViewModel.orderFilter = 0
                }
                1 -> {
                    itemDescending?.isChecked = true
                    alertsViewModel.orderFilter = 1
                }
            }

            // Set first run to false
            DataClass.firstRun = false
        } else {
            // See orderOption and orderFilter in ViewModel, and check the corresponding options
            when (alertsViewModel.orderOption) {
                0 -> itemName?.isChecked = true
                1 -> itemSymbol?.isChecked = true
                2 -> itemPrice?.isChecked = true
                3 -> itemPercentage?.isChecked = true

            }
            when (alertsViewModel.orderFilter) {
                0 -> itemAscending?.isChecked = true
                1 -> itemDescending?.isChecked = true
                else -> {
                }
            }
            // Get the ID of the item selected previously ( of the new one
            alertsViewModel.lastSelectedFilterItem = itemName!!.itemId
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
                if (alertsViewModel.lastSelectedFilterItem == R.id.mnFilterOptionName) {
                    if (itemAscending!!.isChecked) {
                        itemDescending?.isChecked = true
                        alertsViewModel.orderFilter = 1
                    } else {
                        itemAscending?.isChecked = true
                        alertsViewModel.orderFilter = 0
                    }
                } else {
                    item.isChecked = true
                    itemAscending?.isChecked = true
                    alertsViewModel.orderFilter = 0
                }
                alertsViewModel.lastSelectedFilterItem = itemId
                alertsViewModel.orderOption = 0
            }
            R.id.mnFilterOptionSymbol -> {
                if (alertsViewModel.lastSelectedFilterItem == R.id.mnFilterOptionSymbol) {
                    if (itemAscending!!.isChecked) {
                        itemDescending?.isChecked = true
                        alertsViewModel.orderFilter = 1
                    } else {
                        itemAscending?.isChecked = true
                        alertsViewModel.orderFilter = 0
                    }
                } else {
                    item.isChecked = true
                    itemAscending?.isChecked = true
                    alertsViewModel.orderFilter = 0
                }
                alertsViewModel.lastSelectedFilterItem = itemId
                alertsViewModel.orderOption = 1
            }
            R.id.mnFilterOptionPrice -> {
                if (alertsViewModel.lastSelectedFilterItem == R.id.mnFilterOptionPrice) {
                    if (itemAscending!!.isChecked) {
                        itemDescending?.isChecked = true
                        alertsViewModel.orderFilter = 1
                    } else {
                        itemAscending?.isChecked = true
                        alertsViewModel.orderFilter = 0
                    }
                } else {
                    item.isChecked = true
                    itemAscending?.isChecked = true
                    alertsViewModel.orderFilter = 0
                }
                alertsViewModel.lastSelectedFilterItem = itemId
                alertsViewModel.orderOption = 2
            }
            R.id.mnFilterOptionPercentage -> {
                if (alertsViewModel.lastSelectedFilterItem == R.id.mnFilterOptionPercentage) {
                    if (itemAscending!!.isChecked) {
                        itemDescending?.isChecked = true
                        alertsViewModel.orderFilter = 1
                    } else {
                        itemAscending?.isChecked = true
                        alertsViewModel.orderFilter = 0
                    }
                } else {
                    item.isChecked = true
                    itemAscending?.isChecked = true
                    alertsViewModel.orderFilter = 0
                }
                alertsViewModel.lastSelectedFilterItem = itemId
                alertsViewModel.orderOption = 3
            }
            R.id.mnFilterOrderAscending -> {
                itemAscending?.isChecked = true
                alertsViewModel.orderFilter = 0
                alertsViewModel.orderOption = when {
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
                alertsViewModel.orderFilter = 1
                alertsViewModel.orderOption = when {
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
            alertsViewModel.onFilterChanged()
        }
        return true
    }

    private fun initRecyclerView() {
        // Initialize RecyclerView layout manager and adapter
        rwCryptoAdapter = CryptoListAlertsAdapter(requireActivity())
        binding.rwAlertsCryptoList.layoutManager = LinearLayoutManager(context)
        binding.rwAlertsCryptoList.adapter = rwCryptoAdapter
        binding.rwAlertsCryptoList.setHasFixedSize(true)

        // Attach ItemTouchHelper (swipe items to favorite)
        val callback = SimpleItemTouchHelperCallback(rwCryptoAdapter, this, "alerts")
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(binding.rwAlertsCryptoList)
    }

    override fun onSelectedChange(swipingState: Boolean) {
        binding.srlAlertsReload.isEnabled = !swipingState
    }

    private fun setListToAdapter(cryptoList: List<Crypto>) {
        if (cryptoList.isEmpty()) {
            binding.rwAlertsCryptoList.visibility = View.GONE
            binding.tvAlertsCryptoListEmpty.visibility = View.VISIBLE
        } else {
            binding.rwAlertsCryptoList.visibility = View.VISIBLE
            binding.tvAlertsCryptoListEmpty.visibility = View.GONE
            rwCryptoAdapter.setCryptos(cryptoList)
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