package com.kotlin.cryptofication.ui.view

import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.kotlin.cryptofication.BuildConfig
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.utilities.Constants
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.utilities.setCustomButtonStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSettings : PreferenceFragmentCompat() {

    private var lpCurrency: ListPreference? = null
    private var lpFilterOption: ListPreference? = null
    private var lpFilterOrder: ListPreference? = null
    private var lpItemsPage: ListPreference? = null
    private var pAlertTime: Preference? = null
    private var pInfoNotifications: Preference? = null
    private var spScheme: SwitchPreference? = null
    private var pAboutMe: Preference? = null
    private var pCredits: Preference? = null
    private var pVersion: Preference? = null
    private var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        references()

        // Insert custom toolbar
        (activity as AppCompatActivity).supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.toolbar_home)
            elevation = 10f
        }

        loadPreferences()
        preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                Constants.PREF_CURRENCY -> mPrefs.setCurrency(lpCurrency!!.value)
                Constants.PREF_SCHEME -> {
                    mPrefs.setScheme(spScheme!!.isChecked)
                    if (spScheme!!.isChecked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    requireActivity().recreate()
                }
                Constants.PREF_FILTER_OPTION -> mPrefs.setFilterOption(lpFilterOption!!.value)
                Constants.PREF_FILTER_ORDER -> mPrefs.setFilterOrder(lpFilterOrder!!.value)
                Constants.PREF_ITEMS_PAGE -> mPrefs.setItemsPerPage(lpItemsPage!!.value)
            }
        }
        pAlertTime!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val currentAlertTime = mPrefs.getAlertTime().split(":")

                val timePickerDialog = TimePickerDialog(
                    context, { _, hourOfDay, minute ->
                        val newHour: String = if (hourOfDay < 10) {
                            if (hourOfDay == 0) {
                                "00"
                            } else {
                                "0$hourOfDay"
                            }
                        } else {
                            hourOfDay.toString()
                        }
                        val newMinute: String = if (minute < 10) {
                            if (minute == 0) {
                                "00"
                            } else {
                                "0$minute"
                            }
                        } else {
                            minute.toString()
                        }
                        val newTime = "$newHour:$newMinute"
                        mPrefs.setAlertTime(newTime)
                        pAlertTime!!.summary = newTime
                    }, currentAlertTime[0].toInt(), currentAlertTime[1].toInt(), true
                )
                timePickerDialog.show()
                false
            }
        pInfoNotifications!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
                val inflater = requireActivity().layoutInflater
                val dialogView = inflater.inflate(R.layout.dialog_info_notifications, null)
                builder.setView(dialogView)
                builder.setNeutralButton(getString(R.string.CLOSE)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }.create()
                val dialog = builder.show()
                dialog.setCustomButtonStyle()

                // Show the dialog
                dialog.show()
                false
            }
        pAboutMe!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                // Create dialog to confirm the dismiss
                val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
                val inflater = requireActivity().layoutInflater
                val dialogView = inflater.inflate(R.layout.dialog_about, null)
                builder.setView(dialogView)
                builder.setNeutralButton(getString(R.string.CLOSE)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }.create()
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
                val builder = AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
                val inflater = requireActivity().layoutInflater
                val dialogView = inflater.inflate(R.layout.dialog_credits, null)
                builder.setView(dialogView)
                builder.setNeutralButton(getString(R.string.CLOSE)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }.create()
                val dialog = builder.show()
                dialog.setCustomButtonStyle()

                // Show the dialog
                dialog.show()
                false
            }
    }

    private fun references() {
        lpCurrency = findPreference(Constants.PREF_CURRENCY)
        lpFilterOption = findPreference(Constants.PREF_FILTER_OPTION)
        lpFilterOrder = findPreference(Constants.PREF_FILTER_ORDER)
        lpItemsPage = findPreference(Constants.PREF_ITEMS_PAGE)
        pAlertTime = findPreference(Constants.PREF_ALERT_TIME)
        pInfoNotifications = findPreference(Constants.PREF_INFO_NOTIFICATIONS)
        spScheme = findPreference(Constants.PREF_SCHEME)
        pAboutMe = findPreference(Constants.PREF_ABOUT)
        pCredits = findPreference(Constants.PREF_CREDITS)
        pVersion = findPreference(Constants.PREF_VERSION)
    }

    private fun loadPreferences() {
        val listPreferences = mPrefs.getAllPreferences()
        lpCurrency!!.value = listPreferences[0].toString()
        spScheme!!.isChecked = listPreferences[1].toString().toBoolean()
        lpFilterOption!!.value = listPreferences[2].toString()
        lpFilterOrder!!.value = listPreferences[3].toString()
        lpItemsPage!!.value = listPreferences[4].toString()
        pAlertTime!!.summary = listPreferences[5].toString()
        pVersion!!.summary = BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen
            .sharedPreferences!!
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen
            .sharedPreferences!!
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}