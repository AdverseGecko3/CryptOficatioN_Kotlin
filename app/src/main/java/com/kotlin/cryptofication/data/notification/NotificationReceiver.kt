package com.kotlin.cryptofication.data.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.MainActivity
import com.kotlin.cryptofication.utilities.Constants.CHANNEL_ID

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        showNotification(intent, context)
    }

    private fun showNotification(intent: Intent?, context: Context?) {
        val cryptoSymbol = intent?.getStringArrayListExtra("cryptoInfo")?.get(0)
        val cryptoPrice = intent?.getStringArrayListExtra("cryptoInfo")?.get(1)
        val cryptoPercentage = intent?.getStringArrayListExtra("cryptoInfo")?.get(2)
        val text =
            if (cryptoPercentage!!.substring(0, cryptoPercentage.length - 1).toDouble() >= 0) {
                "$cryptoSymbol is up by $cryptoPercentage, currently at $cryptoPrice"
            } else {
                "$cryptoSymbol is down by $cryptoPercentage, currently at $cryptoPrice"
            }

        val i = Intent(context, MainActivity::class.java)
        i.putExtra("lastActivity", "alerts")
        val pi = PendingIntent.getActivity(context, 0, i, 0)

        val notificationBuilder = NotificationCompat.Builder(context!!, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.cryptofication_logo_short_white)
            setContentTitle(context.getString(R.string.app_name))
            setContentText(text)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(pi)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(200, notificationBuilder.build())
        }
    }
}