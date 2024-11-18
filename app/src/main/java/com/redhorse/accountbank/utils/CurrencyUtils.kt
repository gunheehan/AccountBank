package com.redhorse.accountbank.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Int): String {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.KOREA)
    return numberFormat.format(amount)
}
