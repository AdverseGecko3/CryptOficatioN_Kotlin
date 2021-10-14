package com.kotlin.cryptofication.classes

import android.content.Context
import android.content.SharedPreferences
import com.kotlin.cryptofication.R
import java.util.ArrayList

class Preferences(context: Context) {

    private val userPrefs: SharedPreferences = context.getSharedPreferences(
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
        val userPrefsEditor: SharedPreferences.Editor = userPrefs.edit()
        userPrefsEditor.putString(Constants.PREF_CURRENCY, currency)
        userPrefsEditor.apply()
    }

    fun setScheme(scheme: Boolean) {
        val userPrefsEditor: SharedPreferences.Editor = userPrefs.edit()
        userPrefsEditor.putBoolean(Constants.PREF_SCHEME, scheme)
        userPrefsEditor.apply()
    }

    fun setFilterOption(filterOption: String?) {
        val userPrefsEditor: SharedPreferences.Editor = userPrefs.edit()
        userPrefsEditor.putString(Constants.PREF_FILTER_OPTION, filterOption)
        userPrefsEditor.apply()
    }

    fun setFilterOrder(filterOrder: String?) {
        val userPrefsEditor: SharedPreferences.Editor = userPrefs.edit()
        userPrefsEditor.putString(Constants.PREF_FILTER_ORDER, filterOrder)
        userPrefsEditor.apply()
    }

    fun setItemsPerPage(itemsPage: String?) {
        val userPrefsEditor: SharedPreferences.Editor = userPrefs.edit()
        userPrefsEditor.putString(Constants.PREF_ITEMS_PAGE, itemsPage)
        userPrefsEditor.apply()
    }
}