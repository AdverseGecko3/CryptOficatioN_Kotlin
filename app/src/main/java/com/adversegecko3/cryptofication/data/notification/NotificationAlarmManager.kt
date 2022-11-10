package com.adversegecko3.cryptofication.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import java.util.*

class NotificationAlarmManager(context: Context) {
    private val alarmManager: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    private val pendingIntent: PendingIntent = run {
        PendingIntent.getBroadcast(
            mAppContext,
            requestCode,
            Intent(mAppContext, NotificationReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private val requestCode = 9354

    fun launchAlarmManager(newTime: String = "") {
        //deleteAlarmManager()
        val userAlarm = newTime.ifBlank { mPrefs.getAlertTime() }
        val userAlarmParts = userAlarm.split(":")
        val calendarNow = Calendar.getInstance()
        val calendarUser = Calendar.getInstance()
        calendarUser.apply {
            set(Calendar.HOUR_OF_DAY, userAlarmParts[0].toInt())
            set(Calendar.MINUTE, userAlarmParts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Add 1 day if it only remains 60 seconds to the selected hour
        if ((calendarUser.timeInMillis + 60000) < calendarNow.timeInMillis) {
            calendarUser.add(Calendar.DAY_OF_MONTH, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendarUser.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun deleteAlarmManager() {
        alarmManager.cancel(pendingIntent)
    }
}