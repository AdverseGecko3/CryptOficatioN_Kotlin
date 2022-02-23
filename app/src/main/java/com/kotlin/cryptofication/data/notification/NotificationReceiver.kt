package com.kotlin.cryptofication.data.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.data.model.Crypto
import com.kotlin.cryptofication.domain.GetCryptoAlertsOnlineUseCase
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mPrefs
import com.kotlin.cryptofication.ui.view.MainActivity
import com.kotlin.cryptofication.utilities.Constants.CHANNEL_ID
import com.kotlin.cryptofication.utilities.customFormattedPercentage
import com.kotlin.cryptofication.utilities.customFormattedPrice
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class NotificationReceiver : BroadcastReceiver() {

    private val groupKey = "com.kotlin.CryptOficatioN"
    private val getCryptoOnlineUseCase = GetCryptoAlertsOnlineUseCase()

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("NotifServ", "Received!")
        MainScope().launch {
            val cryptoAlerts = loadData()
            showNotification(cryptoAlerts, context)
        }
    }

    private fun showNotification(cryptoList: List<Crypto>, context: Context?) {
        val listNotifications = arrayListOf<Notification>()
        val i = Intent(context, MainActivity::class.java)
        for ((index, crypto: Crypto) in cryptoList.withIndex()) {
            val cryptoSymbol = crypto.symbol?.uppercase()
            Log.d("NotifServ", "Crypto Symbol: $cryptoSymbol")
            val cryptoPrice = crypto.current_price.customFormattedPrice(mPrefs.getCurrencySymbol())
            val cryptoPriceChange =
                crypto.price_change_24h.customFormattedPrice(mPrefs.getCurrencySymbol())
            val cryptoPercentageChange =
                crypto.price_change_percentage_24h.customFormattedPercentage()
            val text =
                if (crypto.price_change_percentage_24h >= 0) {
                    "$cryptoSymbol is $cryptoPercentageChange up ($cryptoPriceChange), currently at $cryptoPrice"
                } else {
                    "$cryptoSymbol is $cryptoPercentageChange down ($cryptoPriceChange), currently at $cryptoPrice"
                }

            Log.d("NotifServ", "alerts${crypto.id}")
            i.putExtra("lastActivity", "alerts${crypto.id}")
            val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    context,
                    index + 1,
                    i,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getActivity(context, index + 1, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val notification = NotificationCompat.Builder(context!!, CHANNEL_ID).apply {
                setSmallIcon(R.drawable.cryptofication_logo_short_white)
                setContentTitle(context.getString(R.string.app_name))
                setContentText(text)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setAutoCancel(true)
                setGroup(groupKey)
                setContentIntent(pi)
            }.build()

            listNotifications.add(notification)
        }

        val notificationSummary = NotificationCompat.Builder(context!!, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.cryptofication_logo_short_white)
            setContentTitle("Summary")
            setContentText("${listNotifications.size} new notifications")
            setGroup(groupKey)
            setGroupSummary(true)
        }.build()

        with(NotificationManagerCompat.from(context)) {
            for ((index, notification: Notification) in listNotifications.withIndex()) {
                notify(200 + (index + 1), notification)
            }
            notify(200, notificationSummary)
        }
    }

    private suspend fun loadData(): List<Crypto> {
        var result: List<Crypto> = emptyList()
        do {
            try {
                // Get Cryptos from the API (online)
                result = getCryptoOnlineUseCase()
                Log.d("NotifServ", "Result: $result")
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            }
        } while (result.isNullOrEmpty())
        return result
    }
}