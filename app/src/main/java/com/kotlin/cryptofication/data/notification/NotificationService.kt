package com.kotlin.cryptofication.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mRoom
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class NotificationService: Service() {

    private var isRunning = false
    private lateinit var mContext: Context
    private lateinit var alarmManager: AlarmManager
    private val alarmPendingIntent by lazy {
        val intent = Intent(mContext, NotificationReceiver::class.java)
        PendingIntent.getBroadcast(mContext, 0, intent, 0)
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        isRunning = true
        alarmManager = mContext.getSystemService(ALARM_SERVICE) as AlarmManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MainScope().launch {
            val cryptoAlerts = mRoom.getAllAlerts()
            val alarmManager: AlarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val i = Intent(mContext, NotificationReceiver::class.java)
            val event = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    /*fun schedulePushNotifications() {
        val calendar = GregorianCalendar.getInstance().apply {
            if (get(Calendar.HOUR_OF_DAY) >= HOUR_TO_SHOW_PUSH) {
                add(Calendar.DAY_OF_MONTH, 1)
            }

            set(Calendar.HOUR_OF_DAY, HOUR_TO_SHOW_PUSH)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmPendingIntent
        )
    }*/

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}