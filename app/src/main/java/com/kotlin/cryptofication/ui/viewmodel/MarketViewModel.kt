package com.kotlin.cryptofication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.cryptofication.data.model.CryptoModel
import com.kotlin.cryptofication.domain.GetCryptoOfflineUseCase
import com.kotlin.cryptofication.domain.GetCryptoOnlineUseCase
import kotlinx.coroutines.launch

class MarketViewModel: ViewModel() {
    val cryptoLiveData = MutableLiveData<List<CryptoModel>>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    var orderOption: Int = 0
    var orderFilter: Int = 0
    var lastSelectedFilterItem: Int = 0

    var getCryptoOnlineUseCase = GetCryptoOnlineUseCase()
    var getCryptoOfflineUseCase = GetCryptoOfflineUseCase()

    fun onCreate() {
        viewModelScope.launch {
            isLoading.postValue(true)

            // Check if variables were saved when returning to FragmentMarket
            Log.d(
                "onCreateViewModel",
                "lastSelectedFilteredItem: $lastSelectedFilterItem " +
                        "- orderOption: $orderOption " +
                        "- orderFilter: $orderFilter"
            )

            var result = getCryptoOnlineUseCase()
            Log.d("MarketViewModel", "Result: $result")
            if (!result.isNullOrEmpty()) {
                result = changeSortRecyclerView(result)
                cryptoLiveData.postValue(result)
                isLoading.postValue(false)
            } else {
                error.postValue("Error while getting cryptos Online!")
                isLoading.postValue(false)
            }
        }
    }

    fun onFilterChanged() {
        isLoading.postValue(true)

        // Check if variables were saved when returning to FragmentMarket
        Log.d(
            "onFilterChangeViewModel",
            "lastSelectedFilteredItem: $lastSelectedFilterItem " +
                    "- orderOption: $orderOption " +
                    "- orderFilter: $orderFilter"
        )

        var result = getCryptoOfflineUseCase()
        Log.d("MarketViewModel", "Result: $result")
        if (!result.isNullOrEmpty()) {
            result = changeSortRecyclerView(result)
            cryptoLiveData.postValue(result)
            isLoading.postValue(false)
        } else {
            error.postValue("Error while getting cryptos Offline!")
            isLoading.postValue(false)
        }
    }

    private fun changeSortRecyclerView(cryptoList: List<CryptoModel>): List<CryptoModel> {
        // Reorder cryptoList
        Log.d("changeSortRecyclerView", "cryptoList[0]: " + cryptoList[0].name)
        Log.d("changeSortRecyclerView", "Type: $orderOption - Order: $orderFilter")
        Log.d("changeSortRecyclerView", "cryptoList size: " + cryptoList.size)
        when (orderOption) {
            0 -> {
                // Ordered by name
                Log.d("changeSortRecyclerView", "Sort by name")
                return if (orderFilter == 0) {
                    cryptoList.sortedBy { crypto -> crypto.name }
                } else {
                    cryptoList.sortedByDescending { crypto -> crypto.name }
                }
            }
            1 -> {
                // Ordered by symbol
                Log.d("changeSortRecyclerView", "Sort by symbol")
                return if (orderFilter == 0) {
                    cryptoList.sortedBy { crypto -> crypto.symbol }
                } else {
                    cryptoList.sortedByDescending { crypto -> crypto.symbol }
                }
            }
            2 -> {
                // Ordered by price
                Log.d("changeSortRecyclerView", "Sort by price")
                return if (orderFilter == 0) {
                    cryptoList.sortedBy { crypto -> crypto.current_price }
                } else {
                    cryptoList.sortedByDescending { crypto -> crypto.current_price }
                }
            }
            3 -> {
                // Ordered by change percentage
                Log.d("changeSortRecyclerView", "Sort by percentage")
                return if (orderFilter == 0) {
                    cryptoList.sortedBy { crypto -> crypto.price_change_percentage_24h }
                } else {
                    cryptoList.sortedByDescending { crypto -> crypto.price_change_percentage_24h }
                }
            }
            else -> {
                return cryptoList
            }
        }
    }
}