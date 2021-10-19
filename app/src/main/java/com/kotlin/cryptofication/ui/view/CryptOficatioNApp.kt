package com.kotlin.cryptofication.ui.view

import android.app.Application
import android.content.Context
import com.kotlin.cryptofication.data.Preferences

class CryptOficatioNApp : Application() {

    companion object {
        lateinit var appContext: Context
        lateinit var prefs: Preferences
    }

    override fun onCreate() {
        super.onCreate()

        // Get application context
        appContext = applicationContext

        // Set a Preferences instance
        prefs = Preferences(applicationContext)
    }


}