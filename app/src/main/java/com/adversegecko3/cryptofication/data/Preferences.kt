package com.adversegecko3.cryptofication.data

import android.content.Context
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mResources
import com.adversegecko3.cryptofication.utilities.Constants

class Preferences(context: Context) {

    private val userPrefs = context.getSharedPreferences(
        context.getString(R.string.PREFERENCES), Context.MODE_PRIVATE
    )!!

    fun getCurrency(): String {
        return userPrefs.getString(Constants.PREF_CURRENCY, "usd")!!
    }

    fun getScheme(): Boolean {
        return userPrefs.getBoolean(Constants.PREF_SCHEME, true)
    }

    fun getFilterOption(): String {
        return userPrefs.getString(Constants.PREF_FILTER_OPTION, "0")!!
    }

    fun getFilterOrder(): String {
        return userPrefs.getString(Constants.PREF_FILTER_ORDER, "0")!!
    }

    fun getItemsPerPage(): String {
        return userPrefs.getString(Constants.PREF_ITEMS_PAGE, "100")!!
    }

    fun getScrollOnPageChanged(): Boolean {
        return userPrefs.getBoolean(Constants.PREF_SCROLL_ON_PAGE_CHANGED, true)
    }

    fun getAlertTime(): String {
        return userPrefs.getString(Constants.PREF_ALERT_TIME, "00:00")!!
    }

    fun getFirstRun(): Boolean {
        return userPrefs.getBoolean(Constants.PREF_FIRST_RUN, true)
    }

    private fun getDBHasItems(): Boolean {
        return userPrefs.getBoolean(Constants.PREF_DB_HAS_ITEMS, false)
    }

    fun getAllPreferences(): List<Any> {
        val preferences: MutableList<Any> = ArrayList()

        return preferences.apply {
            add(userPrefs.getString(Constants.PREF_CURRENCY, "usd")!!)
            add(userPrefs.getBoolean(Constants.PREF_SCHEME, true))
            add(userPrefs.getString(Constants.PREF_FILTER_OPTION, "0")!!)
            add(userPrefs.getString(Constants.PREF_FILTER_ORDER, "0")!!)
            add(userPrefs.getString(Constants.PREF_ITEMS_PAGE, "100")!!)
            add(userPrefs.getBoolean(Constants.PREF_SCROLL_ON_PAGE_CHANGED, true))
            add(userPrefs.getString(Constants.PREF_ALERT_TIME, "00:00")!!)
        }
    }

    fun getCurrencySymbol(): String {
        return when (getCurrency()) {
            "eur" -> mResources.getString(R.string.CURRENCY_EURO)
            "usd" -> mResources.getString(R.string.CURRENCY_DOLLAR)
            else -> mResources.getString(R.string.CURRENCY_DOLLAR)
        }
    }

    fun setCurrency(currency: String?) {
        userPrefs
            .edit()
            .putString(Constants.PREF_CURRENCY, currency)
            .apply()
    }

    fun setScheme(scheme: Boolean) {
        userPrefs
            .edit()
            .putBoolean(Constants.PREF_SCHEME, scheme)
            .apply()
    }

    fun setFilterOption(filterOption: String?) {
        userPrefs
            .edit()
            .putString(Constants.PREF_FILTER_OPTION, filterOption)
            .apply()
    }

    fun setFilterOrder(filterOrder: String?) {
        userPrefs
            .edit()
            .putString(Constants.PREF_FILTER_ORDER, filterOrder)
            .apply()
    }

    fun setItemsPerPage(itemsPage: String?) {
        userPrefs
            .edit()
            .putString(Constants.PREF_ITEMS_PAGE, itemsPage)
            .apply()
    }

    fun setScrollOnPageChanged(scrollOnPageChanged: Boolean) {
        userPrefs
            .edit()
            .putBoolean(Constants.PREF_SCROLL_ON_PAGE_CHANGED, scrollOnPageChanged)
            .apply()
    }

    fun setAlertTime(alertTime: String?) {
        userPrefs
            .edit()
            .putString(Constants.PREF_ALERT_TIME, alertTime)
            .apply()
        if (getDBHasItems()) {
            mAlarmManager.modifyAlarmManager(alertTime!!)
        }
    }

    fun setFirstRun(firstRun: Boolean) {
        userPrefs
            .edit()
            .putBoolean(Constants.PREF_FIRST_RUN, firstRun)
            .apply()
    }

    fun setDBHasItems(DBHasItems: Boolean) {
        userPrefs
            .edit()
            .putBoolean(Constants.PREF_DB_HAS_ITEMS, DBHasItems)
            .apply()
    }
}