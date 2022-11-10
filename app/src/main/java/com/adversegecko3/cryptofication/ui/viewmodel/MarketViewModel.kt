package com.adversegecko3.cryptofication.ui.viewmodel

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.model.CryptoSearch
import com.adversegecko3.cryptofication.domain.GetCryptoDataMarketOnlineUseCase
import com.adversegecko3.cryptofication.domain.GetCryptoMarketOfflineUseCase
import com.adversegecko3.cryptofication.domain.GetCryptoMarketOnlineUseCase
import com.adversegecko3.cryptofication.domain.GetCryptoSearchMarketOnlineUseCase
import com.adversegecko3.cryptofication.utilities.Constants
import com.google.android.gms.ads.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val getCryptoOnlineUseCase: GetCryptoMarketOnlineUseCase,
    private val getCryptoSearchOnlineUseCase: GetCryptoSearchMarketOnlineUseCase,
    private val getCryptoDataMarketOnlineUseCase: GetCryptoDataMarketOnlineUseCase,
    private val getCryptoOfflineUseCase: GetCryptoMarketOfflineUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val cryptoMarketList = MutableLiveData<List<Any>>()
    val cryptoSearchQuery = MutableSharedFlow<List<Any>>()
    val cryptoSearchData = MutableSharedFlow<Crypto>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableSharedFlow<String>()

    var alreadyLaunched = false
    var orderOption = 0
    var orderFilter = 0
    var lastSelectedFilterItem = 0
    var hasAlreadyData = false
    private var page = 1
    var isSearchOpen = false
    var loadType = 1
    var query = ""

    var cryptoList: ArrayList<Any> = arrayListOf()
    var cryptoListSearch: List<Any> = arrayListOf()

    fun onCreate() {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)
            page = 1
            var result: List<Crypto> = emptyList()
            try {
                // Get Cryptos from the API (online)
                result = getCryptoOnlineUseCase()
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            }
            if (result.isNotEmpty()) {
                // Sort cryptoList with the desired filters and post the list
                result = sortCryptoList(result)
                cryptoList = ArrayList(result)
                cryptoMarketList.postValue(result as List<Any>)
                hasAlreadyData = true
            } else {
                // Send error to show a toast
                error.emit("Error while getting cryptos Online!")
            }
            // Stop refreshing
            isLoading.postValue(false)
        }
    }

    fun onLoadNewPages(loadType: Int) {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)
            if (loadType == 0) {
                this@MarketViewModel.loadType = loadType
                page--
            } else {
                this@MarketViewModel.loadType = loadType
                page++
            }
            var result: List<Crypto> = emptyList()
            try {
                // Get Cryptos from the API (online)
                result = getCryptoOnlineUseCase(page)
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            }
            if (result.isNotEmpty()) {
                // Sort cryptoList with the desired filters and post the list
                result = sortCryptoList(result)
                cryptoList = ArrayList(result)
                cryptoMarketList.postValue(result as List<Any>)
                hasAlreadyData = true
            } else {
                // Send error to show a toast
                error.emit("Error while getting cryptos Online!")
            }
            // Stop refreshing
            isLoading.postValue(false)
        }
    }

    fun onFilterChanged() {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)

            // Get Cryptos from the provider (online)
            var result = getCryptoOfflineUseCase()
            if (result.isNotEmpty()) {
                // Sort cryptoList with the desired filters and post the list
                result = sortCryptoList(result)
                cryptoList = ArrayList(result)
                cryptoMarketList.postValue(result as List<Any>)
            } else {
                // Send error to show a toast
                error.emit("Error while getting cryptos Offline!")
            }
            // Stop refreshing
            isLoading.postValue(false)
        }
    }

    fun onSearchQuerySubmit(query: String) {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)
            var result: List<Any> = emptyList()
            try {
                // Get Cryptos from the API (online)
                result = getCryptoSearchOnlineUseCase(query)
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            }
            // Stop refreshing
            if (result.isNotEmpty()) {
                // Sort cryptoList with the desired filters and post the list
                cryptoListSearch = if (result[0] !is String) {
                    ArrayList(result.map { it as CryptoSearch })
                } else {
                    ArrayList(result)
                }
                cryptoSearchQuery.emit(cryptoListSearch)
            } else {
                // Send error to show a toast
                error.emit("Error while searching data!")
            }
            // Stop refreshing
            isLoading.postValue(false)
        }
    }

    fun fetchCryptoSearchClick(selectedCrypto: CryptoSearch) {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)
            var result: List<Crypto> = emptyList()
            try {
                // Get Cryptos from the API (online)
                result = getCryptoDataMarketOnlineUseCase(selectedCrypto.id)
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            }
            // Stop refreshing
            if (result.isNotEmpty()) {
                cryptoSearchData.emit(result[0])
            } else {
                // Send error to show a toast
                error.emit("Error while getting ${selectedCrypto.name} data!")
            }
            // Stop refreshing
            isLoading.postValue(false)
        }
    }

    private fun sortCryptoList(cryptoList: List<Crypto>): List<Crypto> {
        // Reorder cryptoList
        when (orderOption) {
            0 -> {
                // Ordered by change percentage
                return if (orderFilter == 0) {
                    // Order ascending
                    cryptoList.sortedBy { crypto -> crypto.market_cap_rank }
                } else {
                    // Order descending
                    cryptoList.sortedByDescending { crypto -> crypto.market_cap_rank }
                }
            }
            1 -> {
                // Ordered by symbol
                return if (orderFilter == 0) {
                    // Order ascending
                    cryptoList.sortedBy { crypto -> crypto.symbol }
                } else {
                    // Order descending
                    cryptoList.sortedByDescending { crypto -> crypto.symbol }
                }
            }
            2 -> {
                // Ordered by name
                return if (orderFilter == 0) {
                    // Order ascending
                    cryptoList.sortedBy { crypto -> crypto.name }
                } else {
                    // Order descending
                    cryptoList.sortedByDescending { crypto -> crypto.name }
                }
            }
            3 -> {
                // Ordered by price
                return if (orderFilter == 0) {
                    // Order ascending
                    cryptoList.sortedBy { crypto -> crypto.current_price }
                } else {
                    // Order descending
                    cryptoList.sortedByDescending { crypto -> crypto.current_price }
                }
            }
            4 -> {
                // Ordered by change percentage
                return if (orderFilter == 0) {
                    // Order ascending
                    cryptoList.sortedBy { crypto -> crypto.price_change_percentage_24h }
                } else {
                    // Order descending
                    cryptoList.sortedByDescending { crypto -> crypto.price_change_percentage_24h }
                }
            }
            else -> {
                return cryptoList
            }
        }
    }

    fun addBanners() {
        var str = ""
        cryptoList.forEachIndexed { index, item ->
            str += "$index - ${item::class.java.simpleName}\n"
        }

        if (page != 1) cryptoList.add(0, 2.0)
        var i = Constants.ITEMS_PER_AD
        while (i <= cryptoList.size) {
            val adView = AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = resources.getString(R.string.ADMOB_BANNER_RECYCLERVIEW)
            }
            cryptoList.add(i, adView)
            i += Constants.ITEMS_PER_AD
        }
        loadBannerAds()
        cryptoList.add(2.0)

        str = ""
        cryptoList.forEachIndexed { index, item ->
            str += "$index - ${item::class.java.simpleName}\n"
        }
    }

    private fun loadBannerAds() {
        loadBannerAd(Constants.ITEMS_PER_AD)
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

    fun isMiUi(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty())
    }

    private fun getSystemProperty(): String? {
        val propName = "ro.miui.ui.version.name"
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /*
        // Launch autostart activity
        val intent = Intent()
        intent.component = ComponentName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        startActivity(intent)
        */

        return line
    }
}