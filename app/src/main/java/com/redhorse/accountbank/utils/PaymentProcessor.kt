package com.redhorse.accountbank.utils

import android.content.Context
import com.redhorse.accountbank.data.AppDatabase
import com.redhorse.accountbank.data.Payment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

object PaymentProcessor {

    // 메시지를 분석하고 데이터베이스에 저장 및 알림 표시
    suspend fun processPayment(context: Context, message: String) {
        // 정규식으로 결제 정보 파싱
        val payment = RegexUtils.parsePaymentInfo(message, LocalDate.now().toString())

        if(payment.amount < 1)
            return;

        // 결제 정보를 데이터베이스에 저장
        savePayment(context, payment)

        // 알림 표시
        sendNotification(context, payment)
    }

    private suspend fun savePayment(context: Context, payment: Payment) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            db.paymentDao().insert(payment)
        }
    }

    private fun sendNotification(context: Context, payment: Payment) {
        val title = payment.title
        val message = "결제 금액: ${formatCurrency(payment.amount)}원, 타입: ${payment.type}"
        NotificationUtils.showNotification(context, title, message)
    }
}
