package com.kotlin.cryptofication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.data.repos.CryptoAlertRepository
import com.kotlin.cryptofication.domain.GetCryptoAlertsOfflineUseCase
import com.kotlin.cryptofication.domain.GetCryptoAlertsOnlineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val getCryptoOnlineUseCase: GetCryptoAlertsOnlineUseCase,
    private val getCryptoOfflineUseCase: GetCryptoAlertsOfflineUseCase,
    private val mRoom: CryptoAlertRepository
) : ViewModel() {
    val cryptoLiveData = MutableLiveData<List<Any>>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    var alreadyLaunched = false
    var orderOption = 0
    var orderFilter = 0
    var lastSelectedFilterItem = 0

    fun onCreate() {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)
            if (mRoom.getAllAlerts().isNotEmpty()) {
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
                } else {
                    // Send error to show a toast
                    error.postValue("Error while getting cryptos Online!")
                }
            } else {
                cryptoLiveData.postValue(emptyList())
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
}