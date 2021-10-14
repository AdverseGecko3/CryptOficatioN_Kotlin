package com.kotlin.cryptofication.classes

import android.app.Application
import android.content.Context

class CryptOficatioNApp : Application() {

    init {
        appContext = this
    }

    companion object {
        var appContext: CryptOficatioNApp? = null

        fun appContext(): Context {
            return appContext!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext()
    }


}