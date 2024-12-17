package com.redhorse.accountbank.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Int): String {
    val decimalFormat = DecimalFormat("#,###")
    return decimalFormat.format(amount)
}
