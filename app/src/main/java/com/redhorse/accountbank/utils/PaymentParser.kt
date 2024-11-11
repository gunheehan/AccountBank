package com.redhorse.accountbank.utils

import com.redhorse.accountbank.data.Payment

object PaymentParser {
    private val paymentPattern = Regex("""(?:결제|송금|이체)\s+(\d+)(?:원)""") // 결제 관련 패턴 예시

    fun parsePaymentMessage(message: String): Payment? {
        val matchResult = paymentPattern.find(message)
        return if (matchResult != null) {
            val amount = matchResult.groupValues[1].toIntOrNull() ?: 0
            Payment(title = "결제 내역", type = "expense", amount = amount, date = getCurrentDate())
        } else {
            null
        }
    }

    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }
}
