package com.kotlin.cryptofication.ui.view

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kotlin.cryptofication.R

class MainActivity : AppCompatActivity() {

    private var mNavController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize bottomNav and navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.navBottom)
        mNavController = findNavController(R.id.fragmentContainerView)
        bottomNavigation.setupWithNavController(mNavController!!)
        if (intent.getStringExtra("lastActivity") == "splash") {
            Log.d("MainActivity", "Coming from Splash, staying in Market")
        } else if (intent.getStringExtra("lastActivity") == "settings") {
            Log.d("MainActivity", "Coming from Settings, returning to Settings")
            mNavController!!.navigate(R.id.fragmentSettings)
        }
        intent.removeExtra("lastActivity")
    }

    override fun onBackPressed() {
        // Create dialog to confirm the dismiss
        if (mNavController!!.previousBackStackEntry == null) {
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
        } else {
            super.onBackPressed()
        }
    }

    override fun recreate() {
        startActivity(intent.putExtra("lastActivity", "settings"))
        finish()
        overridePendingTransition(R.anim.anim_fade_in_fast, R.anim.anim_fade_out_fast)
    }
}