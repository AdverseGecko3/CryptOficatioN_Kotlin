package com.adversegecko3.cryptofication.ui.view

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.data.Preferences
import com.adversegecko3.cryptofication.data.notification.NotificationAlarmManager
import com.adversegecko3.cryptofication.utilities.Constants.CHANNEL_ID
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CryptOficatioNApp : Application() {

    companion object {
        lateinit var mAppContext: Context
        lateinit var mResources: Resources
        lateinit var mPrefs: Preferences
        lateinit var mAlarmManager: NotificationAlarmManager
    }

    override fun onCreate() {
        super.onCreate()

        // Get application context
        mAppContext = applicationContext

        // Get system resources
        mResources = resources

        // Set a Preferences instance
        mPrefs = Preferences(applicationContext)

        // Create notification channel
        createNotificationChannel()

        // Alarm Manager class
        mAlarmManager = NotificationAlarmManager(applicationContext)

        // Initialize AdMob
        MobileAds.initialize(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
    }
}