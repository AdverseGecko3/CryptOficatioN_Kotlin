package com.kotlin.cryptofication.utilities

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mResources
import java.text.DecimalFormat

fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Double.customFormattedPrice(userCurrency: String, type: Int = 0): String {
    val currencySeparator = DecimalFormat().decimalFormatSymbols.decimalSeparator
    var formattedPercentage = if (type == 0) {
        String.format("%.10f", this)
            .replace("0+$".toRegex(), "")
    } else {
        String.format("%.2f", this)
            .replace("0+$".toRegex(), "")
    }
    if (formattedPercentage.endsWith(currencySeparator)) {
        formattedPercentage = formattedPercentage.substring(0, formattedPercentage.length - 1)
    }
    return "$formattedPercentage$userCurrency"
}

fun Double.customFormattedPercentage(): String {
    val currencySeparator = DecimalFormat().decimalFormatSymbols.decimalSeparator
    var formattedPercentage = String.format("%.2f", this)
        .replace("0+$".toRegex(), "")
    if (formattedPercentage.endsWith(currencySeparator)) {
        formattedPercentage = formattedPercentage.substring(0, formattedPercentage.length - 1)
    }
    return "$formattedPercentage%"
}

fun ImageView.positivePrice() {
    setImageResource(R.drawable.ic_arrow_drop_up)
    setColorFilter(ContextCompat.getColor(this.context, R.color.green_high))
}

fun ImageView.negativePrice() {
    setImageResource(R.drawable.ic_arrow_drop_down)
    setColorFilter(ContextCompat.getColor(this.context, R.color.red_low))
}

fun TextView.positivePrice() {
    setTextColor(ContextCompat.getColor(this.context, R.color.green_high))
}

fun TextView.negativePrice() {
    setTextColor(ContextCompat.getColor(this.context, R.color.red_low))
}

fun RecyclerView.doHaptic() {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator.hasVibrator()) {
        Log.d("doHaptic", "hasVibrator -> SDK: ${Build.VERSION.SDK_INT}")
        when {
            Build.VERSION.SDK_INT >= 30 -> {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            Build.VERSION.SDK_INT >= 26 -> {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        5,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
            else -> {
                vibrator.vibrate(5)
            }
        }
    } else {
        Log.d("doHaptic", "!hasVibrator")
    }
}

fun AlertDialog.setCustomButtonStyle() {
    // Change the button color and weight
    val btnDismiss = getButton(AlertDialog.BUTTON_NEUTRAL)
    btnDismiss.setTextColor(
        ResourcesCompat.getColor(
            mResources,
            R.color.purple_app_accent,
            null
        )
    )

    /*//Force the default buttons to center
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    layoutParams.weight = 1f
    layoutParams.gravity = Gravity.CENTER

    btnDismiss.layoutParams = layoutParams*/
}