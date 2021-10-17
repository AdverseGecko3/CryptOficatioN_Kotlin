package com.kotlin.cryptofication.classes

import android.app.Application
import android.content.Context

class CryptOficatioNApp : Application() {

    companion object {
        lateinit var appContext: Context
        lateinit var prefs: Preferences
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        prefs = Preferences(applicationContext)
    }


}