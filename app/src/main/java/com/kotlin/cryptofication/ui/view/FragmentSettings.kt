package com.kotlin.cryptofication.ui.view

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.utilities.Constants
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.utilities.setCustomButtonStyle
import java.lang.Exception

class FragmentSettings : PreferenceFragmentCompat() {

    private var lpCurrency: ListPreference? = null
    private var lpFilterOption: ListPreference? = null
    private var lpFilterOrder: ListPreference? = null
    private var lpItemsPage: ListPreference? = null
    private var spScheme: SwitchPreference? = null
    private var pAbout: Preference? = null
    private var pCredits: Preference? = null
    private var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        references()

        // Insert custom toolbar
        (requireActivity() as AppCompatActivity).supportActionBar!!.displayOptions =
            ActionBar.DISPLAY_SHOW_CUSTOM
        (requireActivity() as AppCompatActivity).supportActionBar!!.setDisplayShowCustomEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setCustomView(R.layout.toolbar_home)
        (requireActivity() as AppCompatActivity).supportActionBar!!.elevation = 10f

        loadPreferences()
        preferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    Constants.PREF_CURRENCY -> {
                        Log.d(
                            "prefSelected",
                            lpCurrency!!.title.toString() + " - " + lpCurrency!!.value
                        )
                        mPrefs.setCurrency(lpCurrency!!.value)
                    }
                    Constants.PREF_SCHEME -> {
                        Log.d(
                            "prefSelected",
                            spScheme!!.title.toString() + " - " + spScheme!!.isChecked
                        )
                        mPrefs.setScheme(spScheme!!.isChecked)
                        if (spScheme!!.isChecked) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }
                        requireActivity().recreate()
                    }
                    Constants.PREF_FILTER_OPTION -> {
                        Log.d(
                            "prefSelected",
                            lpFilterOption!!.title.toString() + " - " + lpFilterOption!!.value
                        )
                        mPrefs.setFilterOption(lpFilterOption!!.value)
                    }
                    Constants.PREF_FILTER_ORDER -> {
                        Log.d(
                            "prefSelected",
                            lpFilterOrder!!.title.toString() + " - " + lpFilterOrder!!.value
                        )
                        mPrefs.setFilterOrder(lpFilterOrder!!.value)
                    }
                    Constants.PREF_ITEMS_PAGE -> {
                        Log.d(
                            "prefSelected",
                            lpItemsPage!!.title.toString() + " - " + lpItemsPage!!.value
                        )
                        mPrefs.setItemsPerPage(lpItemsPage!!.value)
                    }
                }
            }

        pAbout!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                // Create dialog to confirm the dismiss
                val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
                val inflater = requireActivity().layoutInflater
                val dialogView = inflater.inflate(R.layout.dialog_about, null)
                builder.setView(dialogView)
                builder.setNeutralButton(
                    getString(R.string.CLOSE)
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .create()
                val dialog = builder.show()
                dialog.setCustomButtonStyle()

                val ivLinkedIn = dialog.findViewById<ImageView>(R.id.ivDialogAboutLinkedIn)
                val ivInstagram = dialog.findViewById<ImageView>(R.id.ivDialogAboutInstagram)
                val ivTwitter = dialog.findViewById<ImageView>(R.id.ivDialogAboutTwitter)
                ivLinkedIn?.setOnClickListener {
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("linkedin://in/eric-barrero")
                            )
                        )
                    } catch (e: Exception) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.linkedin.com/in/eric-barrero")
                            )
                        )
                    }
                }
                ivInstagram?.setOnClickListener {
                    val intentInstagram = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/_u/adversegecko3")
                    )
                    intentInstagram.setPackage("com.instagram.android")
                    try {
                        startActivity(intentInstagram)
                    } catch (e: ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://instagram.com/adversegecko3")
                            )
                        )
                    }
                }
                ivTwitter?.setOnClickListener {
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("twitter://user?screen_name=adversegecko3")
                            )
                        )
                    } catch (e: Exception) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://twitter.com/#!/adversegecko3")
                            )
                        )
                    }
                }

                // Show the dialog
                dialog.show()
                false
            }
        pCredits!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                // Create dialog to confirm the dismiss
                val builder = AlertDialog.Builder(
                    requireActivity(),
                    R.style.CustomAlertDialog
                )
                val inflater = requireActivity().layoutInflater
                @SuppressLint("InflateParams") val dialogView =
                    inflater.inflate(R.layout.dialog_credits, null)
                builder.setView(dialogView)
                builder.setNeutralButton(
                    getString(R.string.CLOSE)
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .create()
                val dialog = builder.show()
                dialog.setCustomButtonStyle()

                // Show the dialog
                dialog.show()
                false
            }
    }

    private fun references() {
        lpCurrency = findPreference(Constants.PREF_CURRENCY)
        spScheme = findPreference(Constants.PREF_SCHEME)
        lpFilterOption = findPreference(Constants.PREF_FILTER_OPTION)
        lpFilterOrder = findPreference(Constants.PREF_FILTER_ORDER)
        lpItemsPage = findPreference(Constants.PREF_ITEMS_PAGE)
        pAbout = findPreference(Constants.PREF_ABOUT)
        pCredits = findPreference(Constants.PREF_CREDITS)
    }

    private fun loadPreferences() {
        val listPreferences = mPrefs.getAllPreferences()
        lpCurrency!!.value = listPreferences[0].toString()
        spScheme!!.isChecked = listPreferences[1].toString().toBoolean()
        lpFilterOption!!.value = listPreferences[2].toString()
        lpFilterOrder!!.value = listPreferences[3].toString()
        lpItemsPage!!.value = listPreferences[4].toString()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen
            .sharedPreferences
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen
            .sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}