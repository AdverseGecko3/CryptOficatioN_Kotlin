package com.kotlin.cryptofication.ui.view

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.classes.Constants
import com.kotlin.cryptofication.classes.DataClass
import com.kotlin.cryptofication.classes.DatabaseClass

class MainActivity : AppCompatActivity() {

    private var fragmentShow = 0
    private val fragmentMarket = FragmentMarket()
    private val fragmentFavorites = FragmentFavorites()
    private val fragmentSettings = FragmentSettings()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        references()

        // Initialize database
        DataClass.db = DatabaseClass(this, "CryptOficatioN Database", null, 1)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.navBottom)
        bottomNavigation.setOnItemSelectedListener(navListener)
        if (intent.getStringExtra("lastActivity") == "splash") {
            Log.d("MainActivity", "Coming from Splash, going to Market")
            bottomNavigation.selectedItemId = Constants.MARKETS
            supportFragmentManager.beginTransaction()
                .replace(fragmentShow, FragmentMarket())
                .addToBackStack("market")
                .commit()
        } else if (intent.getStringExtra("lastActivity") == "settings") {
            Log.d("MainActivity", "Coming from Settings, returning to Settings")
            bottomNavigation.selectedItemId = Constants.SETTINGS
            supportFragmentManager.beginTransaction()
                .replace(fragmentShow, FragmentMarket())
                .addToBackStack("market")
                .commit()
            supportFragmentManager.beginTransaction()
                .replace(fragmentShow, FragmentSettings())
                .addToBackStack("settings")
                .commit()
        }
        DataClass.newItem = bottomNavigation.selectedItemId
    }

    private fun references() {
        fragmentShow = R.id.fragmentShow
    }

    private val navListener = NavigationBarView.OnItemSelectedListener { menuItem ->
        var selectedFragment: Fragment? = null
        when (menuItem.itemId) {
            Constants.MARKETS -> {
                selectedFragment = FragmentMarket()
                DataClass.oldItem = DataClass.newItem
                DataClass.newItem = Constants.MARKETS
            }
            Constants.FAVORITES -> {
                selectedFragment = FragmentFavorites()
                DataClass.oldItem = DataClass.newItem
                DataClass.newItem = Constants.FAVORITES
            }
            Constants.SETTINGS -> {
                selectedFragment = FragmentSettings()
                DataClass.oldItem = DataClass.newItem
                DataClass.newItem = Constants.SETTINGS
            }
            else -> {
            }
        }
        fragmentTransaction(selectedFragment)
        true
    }

    private fun fragmentTransaction(selectedFragment: Fragment?) {
        when (DataClass.oldItem) {
            Constants.MARKETS -> when (DataClass.newItem) {
                Constants.MARKETS -> supportFragmentManager.beginTransaction()
                    .replace(fragmentShow, selectedFragment!!)
                    .addToBackStack("market")
                    .commit()
                Constants.FAVORITES, Constants.SETTINGS -> supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                    .replace(fragmentShow, selectedFragment!!)
                    .disallowAddToBackStack()
                    .commit()
                else -> {
                }
            }
            Constants.FAVORITES -> when (DataClass.newItem) {
                Constants.MARKETS -> supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(fragmentShow, selectedFragment!!)
                    .addToBackStack("market")
                    .commit()
                Constants.FAVORITES -> supportFragmentManager.beginTransaction()
                    .replace(fragmentShow, selectedFragment!!)
                    .disallowAddToBackStack()
                    .commit()
                Constants.SETTINGS -> supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                    .replace(fragmentShow, selectedFragment!!)
                    .disallowAddToBackStack()
                    .commit()
                else -> {
                }
            }
            Constants.SETTINGS -> when (DataClass.newItem) {
                Constants.MARKETS, Constants.FAVORITES -> supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(fragmentShow, selectedFragment!!)
                    .disallowAddToBackStack()
                    .commit()
                Constants.SETTINGS -> supportFragmentManager.beginTransaction()
                    .replace(fragmentShow, selectedFragment!!)
                    .disallowAddToBackStack()
                    .commit()
                else -> {
                }
            }
            else -> {
            }
        }
    }

    override fun onBackPressed() {
        // Create dialog to confirm the dismiss
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        val titleExit = TextView(this)
        titleExit.text = getString(R.string.EXIT)
        titleExit.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        titleExit.gravity = Gravity.CENTER
        titleExit.textSize = 25f
        titleExit.setPadding(
            titleExit.lineHeight / 2, titleExit.lineHeight / 2,
            titleExit.lineHeight / 2, titleExit.lineHeight / 2
        )
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
        btnYes.setTextColor(ResourcesCompat.getColor(resources, R.color.purple_app_accent, null))
        btnNo.setTextColor(ResourcesCompat.getColor(resources, R.color.purple_app_accent, null))
        val layoutParams = btnYes.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        btnYes.layoutParams = layoutParams
        btnNo.layoutParams = layoutParams

        // Show the dialog
        dialog.show()
    }

    override fun recreate() {
        startActivity(intent.putExtra("lastActivity", "settings"))
        finish()
        overridePendingTransition(R.anim.anim_fade_in_fast, R.anim.anim_fade_out_fast)
    }
}