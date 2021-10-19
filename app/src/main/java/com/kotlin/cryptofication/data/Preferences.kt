package com.kotlin.cryptofication.data

import android.content.Context
import com.kotlin.cryptofication.R
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

    fun getAllPreferences(): List<Any> {
        val preferences: MutableList<Any> = ArrayList()

        preferences.add(userPrefs.getString(Constants.PREF_CURRENCY, "usd")!!)
        preferences.add(userPrefs.getBoolean(Constants.PREF_SCHEME, true))
        preferences.add(userPrefs.getString(Constants.PREF_FILTER_OPTION, "0")!!)
        preferences.add(userPrefs.getString(Constants.PREF_FILTER_ORDER, "0")!!)
        preferences.add(userPrefs.getString(Constants.PREF_ITEMS_PAGE, "100")!!)
        return preferences
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
}