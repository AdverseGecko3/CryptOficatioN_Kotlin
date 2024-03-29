package com.adversegecko3.cryptofication.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.model.CryptoAlert
import com.adversegecko3.cryptofication.data.repos.CryptoAlertRepository
import com.adversegecko3.cryptofication.domain.GetCryptoAlertsBitcoinUseCase
import com.adversegecko3.cryptofication.domain.GetCryptoAlertsOfflineUseCase
import com.adversegecko3.cryptofication.domain.GetCryptoAlertsOnlineUseCase
import com.adversegecko3.cryptofication.utilities.Constants
import com.google.android.gms.ads.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val getCryptoOnlineUseCase: GetCryptoAlertsOnlineUseCase,
    private val getBitcoinUseCase: GetCryptoAlertsBitcoinUseCase,
    private val getCryptoOfflineUseCase: GetCryptoAlertsOfflineUseCase,
    private val mRoom: CryptoAlertRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val cryptoLiveData = MutableLiveData<List<Any>>()
    val alertsLiveData = MutableLiveData<List<CryptoAlert>>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    var alreadyLaunched = false
    var orderOption = 0
    var orderFilter = 0
    var lastSelectedFilterItem = 0
    var isSearchOpen = false

    var cryptoList: ArrayList<Any> = arrayListOf()

    fun onCreate() {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)

            var cryptoListAlerts = withContext(Dispatchers.IO) { mRoom.getAllAlerts() }
            if (cryptoListAlerts.isNotEmpty()) {
                var result: List<Any> = emptyList()
                var bitcoin: Crypto? = null
                try {
                    // Get Cryptos from the API (online)
                    result = getCryptoOnlineUseCase()
                    bitcoin = getBitcoinUseCase()
                } catch (e: SocketTimeoutException) {
                    e.printStackTrace()
                }
                if (result.isNotEmpty()) {
                    // Sort API and DB crypto to match IDs
                    result = quickSortCrypto(result).map { it as Crypto }
                    cryptoListAlerts = quickSortCrypto(cryptoListAlerts).map { it as CryptoAlert }
                    cryptoListAlerts =
                        updateCryptoListAlertsValues(cryptoListAlerts.toMutableList(), result)

                    // Sort cryptoList with the desired filters and post the list
                    result = sortCryptoList(result)
                    cryptoListAlerts = sortCryptoPortfolio(result, cryptoListAlerts)
                    cryptoLiveData.postValue(result as List<Any>)
                    alertsLiveData.postValue(
                        addBitcoinToLast(
                            cryptoListAlerts.toMutableList(),
                            bitcoin
                        )
                    )
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

    private suspend fun updateCryptoListAlertsValues(
        cryptoListAlerts: MutableList<CryptoAlert>,
        result: List<Crypto>
    ): List<CryptoAlert> {
        // Update DB values
        for ((index, crypto) in result.withIndex()) {
            val cryptoAlert = CryptoAlert(
                crypto.id,
                crypto.symbol,
                crypto.current_price,
                cryptoListAlerts[index].quantity
            )
            mRoom.modifyQuantityAlert(cryptoAlert)
            cryptoListAlerts[index] = cryptoAlert
        }
        return cryptoListAlerts
    }

    private fun addBitcoinToLast(
        cryptoList: MutableList<CryptoAlert>,
        bitcoin: Crypto?
    ): List<CryptoAlert> {
        return cryptoList
            .apply {
                add(
                    CryptoAlert(
                        bitcoin!!.id,
                        bitcoin.symbol,
                        bitcoin.current_price,
                        0.0
                    )
                )
                toList()
            }
    }

    fun onAlertsUpdated() {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)

            var cryptoListAlerts: List<Any> = withContext(Dispatchers.IO) { mRoom.getAllAlerts() }
            if (cryptoListAlerts.isNotEmpty()) {
                // Sort API and DB crypto to match IDs
                var result = quickSortCrypto(getCryptoOfflineUseCase()).map { it as Crypto }
                val bitcoin = getBitcoinUseCase()
                cryptoListAlerts = quickSortCrypto(cryptoListAlerts).map { it as CryptoAlert }

                // Sort cryptoList with the desired filters and post the list
                result = sortCryptoList(result)
                cryptoListAlerts = sortCryptoPortfolio(result, cryptoListAlerts)
                alertsLiveData.postValue(
                    addBitcoinToLast(
                        cryptoListAlerts.toMutableList(),
                        bitcoin
                    )
                )
            }
            // Stop refreshing
            isLoading.postValue(false)
        }
    }

    fun onFilterChanged() {
        viewModelScope.launch {
            // Start refreshing
            isLoading.postValue(true)

            var cryptoListAlerts = withContext(Dispatchers.IO) { mRoom.getAllAlerts() }
            if (cryptoListAlerts.isNotEmpty()) {
                // Get Cryptos from the provider (online)
                var result = getCryptoOfflineUseCase()
                val bitcoin = getBitcoinUseCase()
                if (result.isNotEmpty()) {
                    // Sort cryptoList with the desired filters and post the list
                    result = sortCryptoList(result)
                    cryptoListAlerts = sortCryptoPortfolio(result, cryptoListAlerts)
                    cryptoLiveData.postValue(result as List<Any>)
                    alertsLiveData.postValue(
                        addBitcoinToLast(
                            cryptoListAlerts.toMutableList(),
                            bitcoin
                        )
                    )
                    // Stop refreshing
                    isLoading.postValue(false)
                } else {
                    // Send error to show a toast
                    error.postValue("Error while getting cryptos Offline!")

                    // Stop refreshing
                    isLoading.postValue(false)
                }
            }
        }
    }

    private fun quickSortCrypto(cryptoList: List<Any>): List<Any> {
        // Reorder cryptoList
        return when (cryptoList[0]) {
            is Crypto -> (cryptoList.map { it as Crypto }).sortedBy { crypto -> crypto.id }
            else -> (cryptoList.map { it as CryptoAlert }).sortedBy { crypto -> crypto.id }
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

    private fun sortCryptoPortfolio(
        cryptoListAPI: List<Crypto>,
        cryptoListPortfolio: List<CryptoAlert>
    ): List<CryptoAlert> {
        val sortedCryptoListPortfolio: ArrayList<CryptoAlert> = arrayListOf()
        for (crypto in cryptoListAPI) {
            for (cryptoPortfolio in cryptoListPortfolio) {
                if (crypto.id == cryptoPortfolio.id) {
                    sortedCryptoListPortfolio.add(cryptoPortfolio)
                    break
                }
            }
        }
        return sortedCryptoListPortfolio
    }

    fun updateCryptoAlert(cryptoAlert: CryptoAlert) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { mRoom.modifyQuantityAlert(cryptoAlert) }
        }
    }

    fun addBanners() {
        var i = 0
        while (i <= cryptoList.size) {
            val adView = AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = resources.getString(R.string.ADMOB_BANNER_RECYCLERVIEW)
            }
            cryptoList.add(i, adView)
            i += Constants.ITEMS_PER_AD
        }
        loadBannerAds()
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
}