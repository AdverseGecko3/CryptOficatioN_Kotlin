package com.kotlin.cryptofication.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (CryptOficatioNApp.mPrefs.getScheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        setTheme(R.style.Theme_CryptOficatioNKotlin)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize navController and bottomNav
        val bottomNavView = binding.navBottom
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.my_nav)
        navController.graph = navGraph

        if (intent.getStringExtra("lastActivity") != null) {
            intent.getStringExtra("lastActivity")?.let {
                when {
                    it == "settings" -> {
                        // Settings extra, coming from Settings, returning to Settings
                        navGraph.setStartDestination(R.id.navigationFragmentSettings)
                        bottomNavView.setupWithNavController(navController)
                        navController.navigate(R.id.navigationFragmentSettings)
                    }
                    it.contains("alerts") -> {
                        // Alerts extra, coming from Notification, going to Alerts
                        val cryptoId = it.substring(6, it.length)
                        val cryptoBundle = bundleOf("cryptoId" to cryptoId)
                        navGraph.setStartDestination(R.id.navigationFragmentAlerts)
                        bottomNavView.setupWithNavController(navController)
                        navController.navigate(R.id.navigationFragmentAlerts, cryptoBundle)
                    }
                    else -> {
                        // Unknown extra
                        navGraph.setStartDestination(R.id.navigationFragmentMarket)
                        bottomNavView.setupWithNavController(navController)
                    }
                }
                intent?.removeExtra("lastActivity")
            }

        } else {
            // No extras
            navGraph.setStartDestination(R.id.navigationFragmentMarket)
            bottomNavView.setupWithNavController(navController)
        }
    }

    override fun onBackPressed() {
        // Create dialog to confirm the dismiss
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        val titleExit = TextView(this).apply {
            text = getString(R.string.EXIT)
            setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
            gravity = Gravity.CENTER
            textSize = 25f
            setPadding(
                lineHeight / 2, lineHeight / 2,
                lineHeight / 2, lineHeight / 2
            )
        }
        builder.setCustomTitle(titleExit)
            .setMessage(getString(R.string.CONFIRMATION_EXIT))
            .setNegativeButton(
                getString(R.string.NO)
            ) { dialog, _ -> dialog.cancel() }
            .setPositiveButton(
                getString(R.string.YES)
            ) { _, _ -> super.onBackPressed() }
            .create()
        val dialog = builder.show()

        // Change the buttons color and weight
        val btnYes = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNo = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        btnYes.setTextColor(
            ResourcesCompat.getColor(
                resources,
                R.color.purple_app_accent,
                null
            )
        )
        btnNo.setTextColor(ResourcesCompat.getColor(resources, R.color.purple_app_accent, null))
        val layoutParams = btnYes.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        btnYes.layoutParams = layoutParams
        btnNo.layoutParams = layoutParams

        // Show the dialog
        dialog.show()
    }

    override fun recreate() {
        //finish()
        startActivity(Intent(this, javaClass).putExtra("lastActivity", "settings"))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}