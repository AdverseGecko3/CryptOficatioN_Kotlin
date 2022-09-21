package com.kotlin.cryptofication.data.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getCryptoOnlineUseCase: GetCryptoAlertsOnlineUseCase

    private val groupKey = "com.kotlin.CryptOficatioN"

    override fun onReceive(context: Context?, intent: Intent?) {
        CoroutineScope(Dispatchers.Main).launch {
            val cryptoAlerts = loadData()
            showNotification(cryptoAlerts, context)
        }
    }

    private fun showNotification(cryptoList: List<Crypto>, context: Context?) {
        val listNotifications = arrayListOf<Notification>()
        val i = Intent(context, MainActivity::class.java)
        for ((index, crypto: Crypto) in cryptoList.withIndex()) {
            val cryptoSymbol = crypto.symbol.uppercase()
            val cryptoPrice = crypto.current_price.customFormattedPrice(mPrefs.getCurrencySymbol())
            val cryptoPriceChange =
                crypto.price_change_24h.customFormattedPrice(mPrefs.getCurrencySymbol())
            val cryptoPercentageChange =
                crypto.price_change_percentage_24h.customFormattedPercentage()
            var title: String
            var body: String
            if (crypto.price_change_percentage_24h >= 0) {
                title = "\uD83D\uDCC8 $cryptoSymbol \uD83D\uDCC8"
                body =
                    "${crypto.name} is up by $cryptoPercentageChange ($cryptoPriceChange), currently at $cryptoPrice"
            } else {
                title = "\uD83D\uDCC9 $cryptoSymbol \uD83D\uDCC9"
                body =
                    "${crypto.name} is down by $cryptoPercentageChange ($cryptoPriceChange), currently at $cryptoPrice"
            }

            i.putExtra("lastActivity", "alerts${crypto.id}")
            val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    context,
                    index + 1,
                    i,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            } else {
                @Suppress("UnspecifiedImmutableFlag")
                PendingIntent.getActivity(context, index + 1, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val notification = NotificationCompat.Builder(context!!, CHANNEL_ID).apply {
                setSmallIcon(R.drawable.cryptofication_logo_short_white)
                setContentTitle(title)
                setStyle(NotificationCompat.BigTextStyle().bigText(body))
                setContentText(body)
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
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            }
        } while (result.isEmpty())
        return result
    }
}