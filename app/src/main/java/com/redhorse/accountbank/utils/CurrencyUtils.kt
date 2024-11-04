package com.redhorse.accountbank.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Int): String {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("ko", "KR"))
    return numberFormat.format(amount)
}
