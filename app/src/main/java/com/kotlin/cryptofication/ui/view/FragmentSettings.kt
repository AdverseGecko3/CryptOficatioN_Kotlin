package com.kotlin.cryptofication.ui.view

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.classes.Constants
import com.kotlin.cryptofication.classes.CryptOficatioNApp
import com.kotlin.cryptofication.classes.Preferences
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
    private val preferences = Preferences(CryptOficatioNApp.appContext())
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
            SharedPreferences.OnSharedPreferenceChangeListener { _: SharedPreferences?, key: String? ->
                when (key) {
                    Constants.PREF_CURRENCY -> {
                        Log.d(
                            "prefSelected",
                            lpCurrency!!.title.toString() + " - " + lpCurrency!!.value
                        )
                        preferences.setCurrency(lpCurrency!!.value)
                    }
                    Constants.PREF_SCHEME -> {
                        Log.d(
                            "prefSelected",
                            spScheme!!.title.toString() + " - " + spScheme!!.isChecked
                        )
                        preferences.setScheme(spScheme!!.isChecked)
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
                        preferences.setFilterOption(lpFilterOption!!.value)
                    }
                    Constants.PREF_FILTER_ORDER -> {
                        Log.d(
                            "prefSelected",
                            lpFilterOrder!!.title.toString() + " - " + lpFilterOrder!!.value
                        )
                        preferences.setFilterOrder(lpFilterOrder!!.value)
                    }
                    Constants.PREF_ITEMS_PAGE -> {
                        Log.d(
                            "prefSelected",
                            lpItemsPage!!.title.toString() + " - " + lpItemsPage!!.value
                        )
                        preferences.setItemsPerPage(lpItemsPage!!.value)
                    }
                    else -> {
                    }
                }
            }
        pAbout!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                // Create dialog to confirm the dismiss
                val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
                val inflater: LayoutInflater = requireActivity().layoutInflater
                val dialogView: View =
                    inflater.inflate(R.layout.dialog_about, null)
                builder.setView(dialogView)
                builder.setNeutralButton(
                    getString(R.string.CLOSE)
                ) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                    .create()
                val dialog = builder.show()

                // Change the button color and weight
                val btnDismiss = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                btnDismiss.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.purple_app_accent,
                        null
                    )
                )
                val layoutParams: LinearLayout.LayoutParams =
                    btnDismiss.layoutParams as LinearLayout.LayoutParams
                layoutParams.weight = 10f
                btnDismiss.layoutParams = layoutParams
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
                val inflater: LayoutInflater = requireActivity().layoutInflater
                @SuppressLint("InflateParams") val dialogView: View =
                    inflater.inflate(R.layout.dialog_credits, null)
                builder.setView(dialogView)
                builder.setNeutralButton(
                    getString(R.string.CLOSE)
                ) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                    .create()
                val dialog = builder.show()

                // Change the button color and weight
                val btnDismiss = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                btnDismiss.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.purple_app_accent,
                        null
                    )
                )
                val layoutParams: LinearLayout.LayoutParams =
                    btnDismiss.layoutParams as LinearLayout.LayoutParams
                layoutParams.weight = 10f
                btnDismiss.layoutParams = layoutParams

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
        val listPreferences: List<Any> = preferences.getAllPreferences()
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