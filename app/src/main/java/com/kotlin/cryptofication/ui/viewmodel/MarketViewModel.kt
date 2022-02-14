package com.kotlin.cryptofication.ui.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.domain.GetCryptoMarketOfflineUseCase
import com.kotlin.cryptofication.domain.GetCryptoMarketOnlineUseCase
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.SocketTimeoutException

class MarketViewModel: ViewModel() {
    val cryptoLiveData = MutableLiveData<List<Any>>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    var alreadyLaunched = false
    var orderOption = 0
    var orderFilter = 0
    var lastSelectedFilterItem = 0
    var hasAlreadyData = false

    var getCryptoOnlineUseCase = GetCryptoMarketOnlineUseCase()
    var getCryptoOfflineUseCase = GetCryptoMarketOfflineUseCase()

    fun onCreate() {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)
            var result: List<Crypto> = emptyList()
            try {
                // Get Cryptos from the API (online)
                result = getCryptoOnlineUseCase()
                Log.d("onCreateViewModel", "Result: $result")
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
        Log.d("changeSortRecyclerView", "cryptoList[0]: " + cryptoList[0].name)
        Log.d("changeSortRecyclerView", "Type: $orderOption - Order: $orderFilter")
        Log.d("changeSortRecyclerView", "cryptoList size: " + cryptoList.size)
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

    fun isMiUi(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))
    }

    private fun getSystemProperty(propName: String): String? {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            Log.d("marketVM-Xiaomi", line)
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