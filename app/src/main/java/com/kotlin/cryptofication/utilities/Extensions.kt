package com.kotlin.cryptofication.utilities

import android.content.Context
import android.os.*
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mResources
import java.text.DecimalFormat

fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Double.customFormattedPrice(userCurrency: String, twoDecimalFormat: Boolean = false): String {
    val currencySeparator = '.'
    val currencySeparatorLocale = DecimalFormat().decimalFormatSymbols.decimalSeparator

    var formattedPrice = when {
        twoDecimalFormat || this >= 100 -> {
            String.format("%.2f", this).replace("0+$".toRegex(), "")
        }
        this >= 1 -> String.format("%.3f", this).replace("0+$".toRegex(), "")
        else -> {
            val priceAfterSeparator =
                this.toBigDecimal().toPlainString().split(currencySeparator)[1]
            val leadingZeros =
                priceAfterSeparator.length - priceAfterSeparator.replace("^0+".toRegex(), "").length
            String.format("%.${leadingZeros + 4}f", this).replace("0+$".toRegex(), "")
        }
    }
    if (formattedPrice.endsWith(currencySeparatorLocale)) {
        formattedPrice = formattedPrice.substring(0, formattedPrice.length - 1)
    }
    return "$formattedPrice$userCurrency"
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

fun Double.formattedDouble(): String {
    val currencySeparator = '.'
    val currencySeparatorLocale = DecimalFormat().decimalFormatSymbols.decimalSeparator

    var formattedDouble = when {
        this >= 100 -> String.format("%.2f", this).replace("0+$".toRegex(), "")
        this >= 1 -> String.format("%.3f", this).replace("0+$".toRegex(), "")
        else -> {
            val priceAfterSeparator =
                this.toBigDecimal().toPlainString().split(currencySeparator)[1]
            val leadingZeros =
                priceAfterSeparator.length - priceAfterSeparator.replace("^0+".toRegex(), "").length
            String.format("%.${leadingZeros + 4}f", this).replace("0+$".toRegex(), "")
        }
    }
    if (formattedDouble.endsWith(currencySeparatorLocale)) {
        formattedDouble = formattedDouble.substring(0, formattedDouble.length - 1)
    }
    return formattedDouble
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
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (vibrator.hasVibrator()) {
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
                @Suppress("DEPRECATION")
                vibrator.vibrate(5)
            }
        }
    }
}

fun AlertDialog.setCustomButtonStyle(type: Int = 0) {
    // Change the button color and weight
    val btnDismiss = getButton(AlertDialog.BUTTON_NEUTRAL)
    btnDismiss.setTextColor(
        ResourcesCompat.getColor(
            mResources,
            R.color.purple_app_accent,
            null
        )
    )

    if (type == 1) {
        val btnAccept = getButton(AlertDialog.BUTTON_POSITIVE)
        btnAccept.setTextColor(
            ResourcesCompat.getColor(
                mResources,
                R.color.purple_app_accent,
                null
            )
        )
    }

    /*//Force the default buttons to center
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    layoutParams.weight = 1f
    layoutParams.gravity = Gravity.CENTER

    btnDismiss.layoutParams = layoutParams*/
}

fun View.below(view: View) {
    (this.layoutParams as? RelativeLayout.LayoutParams)?.addRule(RelativeLayout.BELOW, view.id)
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}