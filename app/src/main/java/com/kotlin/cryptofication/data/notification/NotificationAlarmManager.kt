package com.kotlin.cryptofication.data.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
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
            @Suppress("UnspecifiedImmutableFlag")
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
        calendarUser.apply {
            set(Calendar.HOUR_OF_DAY, userAlarmParts[0].toInt())
            set(Calendar.MINUTE, userAlarmParts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if ((calendarUser.timeInMillis + 60000) < calendarNow.timeInMillis) {
            // Add 1 day if it only remains 1 minute to the selected hour
            calendarUser.add(Calendar.DAY_OF_MONTH, 1)
        }

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
        calendarUser.apply {
            set(Calendar.HOUR_OF_DAY, userAlarmParts[0].toInt())
            set(Calendar.MINUTE, userAlarmParts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if ((calendarUser.timeInMillis + 5000) < calendarNow.timeInMillis) {
            // Add 1 day if it only remains 1 minute to the selected hour
            calendarUser.add(Calendar.DAY_OF_MONTH, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendarUser.timeInMillis,
            1000 * 60 * 60 * 24,
            pendingIntent
        )
    }

    fun deleteAlarmManager() {
        alarmManager.cancel(pendingIntent)
    }
}