package com.kotlin.cryptofication.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mRoom
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BootUpReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            MainScope().launch {
                mRoom.getAllAlerts().let {
                    if (!it.isNullOrEmpty()) {
                        val i = Intent(context, NotificationService::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context?.startActivity(i)
                    }
                }
            }
        }
    }
}