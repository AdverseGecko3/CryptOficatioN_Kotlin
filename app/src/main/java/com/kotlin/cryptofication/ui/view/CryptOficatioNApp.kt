package com.kotlin.cryptofication.ui.view

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.kotlin.cryptofication.data.Preferences
import com.kotlin.cryptofication.data.repos.CryptoAlertRepository

class CryptOficatioNApp : Application() {

    companion object {
        lateinit var mAppContext: Context
        lateinit var mResources: Resources
        lateinit var mPrefs: Preferences
        lateinit var mRoom: CryptoAlertRepository
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
    }
}