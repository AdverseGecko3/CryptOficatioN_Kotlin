package com.kotlin.cryptofication.data.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import java.util.*

class NotificationAlarmManager(context: Context) {
    private val alarmManager: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
    private val pendingIntent: PendingIntent = run {
        val i = Intent(mAppContext, NotificationReceiver::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(mAppContext, requestCode, i, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(
                mAppContext,
                requestCode,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
    private val requestCode = 9354

    @SuppressLint("SimpleDateFormat")
    fun launchAlarmManager() {
        //deleteAlarmManager()
        val userAlarm = mPrefs.getAlertTime()
        val userAlarmParts = userAlarm.split(":")
        val calendarNow = Calendar.getInstance()
        val calendarUser = Calendar.getInstance()
        calendarUser.set(Calendar.HOUR_OF_DAY, userAlarmParts[0].toInt())
        calendarUser.set(Calendar.MINUTE, userAlarmParts[1].toInt())
        calendarUser.set(Calendar.SECOND, 0)
        calendarUser.set(Calendar.MILLISECOND, 0)

        Log.d("NotifServ", "${calendarNow.time} - ${calendarUser.time}")

        if ((calendarUser.timeInMillis + 5000) < calendarNow.timeInMillis) {
            calendarUser.add(Calendar.DAY_OF_MONTH, 1)
            Log.d("NotifServ", "Added 1 day")
            Log.d("NotifServ", "${calendarNow.time} - ${calendarUser.time}")
            Log.d(
                "NotifServ",
                "${calendarNow.timeInMillis} - ${calendarUser.timeInMillis}"
            )
        }

        Log.d("NotifServ", "Tamo activo")

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendarUser.timeInMillis,
            1000 * 60 * 60 * 24,
            pendingIntent
        )
    }

    @SuppressLint("SimpleDateFormat")
    fun modifyAlarmManager(newTime: String) {
        //deleteAlarmManager()
        val userAlarmParts = newTime.split(":")
        val calendarNow = Calendar.getInstance()
        val calendarUser = Calendar.getInstance()
        calendarUser.set(Calendar.HOUR_OF_DAY, userAlarmParts[0].toInt())
        calendarUser.set(Calendar.MINUTE, userAlarmParts[1].toInt())
        calendarUser.set(Calendar.SECOND, 0)
        calendarUser.set(Calendar.MILLISECOND, 0)

        Log.d("NotifServ", "${calendarNow.time} - ${calendarUser.time}")

        if ((calendarUser.timeInMillis + 5000) < calendarNow.timeInMillis) {
            calendarUser.add(Calendar.DAY_OF_MONTH, 1)
            Log.d("NotifServ", "Added 1 day")
            Log.d("NotifServ", "${calendarNow.time} - ${calendarUser.time}")
            Log.d(
                "NotifServ",
                "${calendarNow.timeInMillis} - ${calendarUser.timeInMillis}"
            )
        }

        Log.d("NotifServ", "Tamo cambiao")

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendarUser.timeInMillis,
            1000 * 60 * 60 * 24,
            pendingIntent
        )
    }

    fun deleteAlarmManager() {
        Log.d("NotifServ", "Tamo out")
        alarmManager.cancel(pendingIntent)
    }
}