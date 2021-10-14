package com.kotlin.cryptofication.ui.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatDelegate
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.classes.CryptOficatioNApp
import com.kotlin.cryptofication.classes.Preferences
import com.kotlin.cryptofication.databinding.ActivitySplashBinding

class SplashActivity : Activity() {

    private val splashDuration = 2000
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Preferences(CryptOficatioNApp.appContext()).getScheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            this.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val anim = AnimationUtils.loadAnimation(this, R.anim.anim_splash)
        binding.ivSplashLogo.animation = anim
        binding.tvSplashPoweredBy.animation = anim
        val intent = Intent(this, MainActivity::class.java)
        val timer: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(splashDuration.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    startActivity(intent.putExtra("lastActivity", "splash"))
                    overridePendingTransition(R.anim.anim_fade_in_slow, R.anim.anim_fade_out_slow)
                    finish()
                }
            }
        }
        timer.start()
    }
}