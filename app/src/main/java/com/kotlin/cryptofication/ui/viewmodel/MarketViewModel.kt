package com.kotlin.cryptofication.ui.viewmodel

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.*
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.domain.GetCryptoMarketOfflineUseCase
import com.kotlin.cryptofication.domain.GetCryptoMarketOnlineUseCase
import com.kotlin.cryptofication.utilities.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val getCryptoOnlineUseCase: GetCryptoMarketOnlineUseCase,
    private val getCryptoOfflineUseCase: GetCryptoMarketOfflineUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val cryptoLiveData = MutableLiveData<List<Any>>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    var alreadyLaunched = false
    var orderOption = 0
    var orderFilter = 0
    var lastSelectedFilterItem = 0
    var hasAlreadyData = false
    private var page = 1

    var cryptoList: ArrayList<Any> = arrayListOf()

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
            if (!result.isNullOrEmpty()) {
                // Sort cryptoList with the desired filters and post the list
                result = sortCryptoList(result)
                cryptoLiveData.postValue(result as List<Any>)
                hasAlreadyData = true
            } else {
                // Send error to show a toast
                error.postValue("Error while getting cryptos Online!")
            }
            // Stop refreshing
            isLoading.postValue(false)
        }
    }

    fun onLoadMorePages() {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)
            page++
            var result: List<Crypto> = emptyList()
            try {
                // Get Cryptos from the API (online)
                result = getCryptoOnlineUseCase(page)
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            }
            if (!result.isNullOrEmpty()) {
                // Sort cryptoList with the desired filters and post the list
                result = sortCryptoList(result)
                cryptoLiveData.postValue(result as List<Any>)
                hasAlreadyData = true
            } else {
                // Send error to show a toast
                error.postValue("Error while getting cryptos Online!")
            }
            // Stop refreshing
            isLoading.postValue(false)
        }
    }

    fun onFilterChanged() {
        // Start refreshing
        isLoading.postValue(true)

        // Get Cryptos from the provider (online)
        var result = getCryptoOfflineUseCase()
        if (!result.isNullOrEmpty()) {
            // Sort cryptoList with the desired filters and post the list
            result = sortCryptoList(result)
            cryptoLiveData.postValue(result as List<Any>)

            // Stop refreshing
            isLoading.postValue(false)
        } else {
            // Send error to show a toast
            error.postValue("Error while getting cryptos Offline!")

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
        var i = 0
        while (i <= cryptoList.size) {
            val adView = AdView(context).apply {
                adSize = AdSize.BANNER
                adUnitId = resources.getString(R.string.ADMOB_BANNER_RECYCLERVIEW)
            }
            cryptoList.add(i, adView)
            i += Constants.ITEMS_PER_AD
        }
        loadBannerAds()
        cryptoList.add(Double)
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