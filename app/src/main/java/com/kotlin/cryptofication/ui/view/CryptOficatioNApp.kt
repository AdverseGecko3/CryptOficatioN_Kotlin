package com.kotlin.cryptofication.ui.view

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.data.notification.NotificationAlarmManager
import com.kotlin.cryptofication.data.Preferences
import com.kotlin.cryptofication.data.repos.CryptoAlertRepository
import com.kotlin.cryptofication.utilities.Constants.CHANNEL_ID

class CryptOficatioNApp : Application() {

    companion object {
        lateinit var mAppContext: Context
        lateinit var mResources: Resources
        lateinit var mPrefs: Preferences
        lateinit var mRoom: CryptoAlertRepository
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

        // Create Room
        mRoom = CryptoAlertRepository(applicationContext as Application)

        // Create notification channel
        createNotificationChannel()

        // Alarm Manager class
        mAlarmManager = NotificationAlarmManager(applicationContext)
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