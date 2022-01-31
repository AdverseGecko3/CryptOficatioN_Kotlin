package com.kotlin.cryptofication.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mRoom
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BootUpReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NotifServ", "onReceive")
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("NotifServ", "Boot Completed")
            MainScope().launch {
                mRoom.getAllAlerts().let {
                    if (!it.isNullOrEmpty()) {
                        Log.d("NotifServ", "Alerts not null or empty")
                        mAlarmManager.launchAlarmManager()
                    }
                }
            }
        }
    }
}