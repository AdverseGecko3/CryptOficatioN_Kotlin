package com.adversegecko3.cryptofication.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.adversegecko3.cryptofication.data.repos.CryptoAlertRepository
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mAlarmManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootUpReceiver : BroadcastReceiver() {

    @Inject
    lateinit var mRoom: CryptoAlertRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            MainScope().launch {
                mRoom.getAllAlerts().let {
                    if (it.isNotEmpty()) {
                        mAlarmManager.launchAlarmManager()
                    }
                }
            }
        }
    }
}