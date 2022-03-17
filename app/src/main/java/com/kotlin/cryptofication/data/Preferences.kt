package com.kotlin.cryptofication.data

import android.content.Context
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mResources
import com.kotlin.cryptofication.utilities.Constants
import java.util.ArrayList

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

        preferences.add(userPrefs.getString(Constants.PREF_CURRENCY, "usd")!!)
        preferences.add(userPrefs.getBoolean(Constants.PREF_SCHEME, true))
        preferences.add(userPrefs.getString(Constants.PREF_FILTER_OPTION, "0")!!)
        preferences.add(userPrefs.getString(Constants.PREF_FILTER_ORDER, "0")!!)
        preferences.add(userPrefs.getString(Constants.PREF_ITEMS_PAGE, "100")!!)
        preferences.add(userPrefs.getString(Constants.PREF_ALERT_TIME, "00:00")!!)
        return preferences
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

    fun setAlertTime(alertTime: String?) {
        if (getDBHasItems()) {
            mAlarmManager.modifyAlarmManager(alertTime!!)
        }
        userPrefs
            .edit()
            .putString(Constants.PREF_ALERT_TIME, alertTime)
            .apply()
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