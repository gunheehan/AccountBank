package com.redhorse.accountbank.utils

import RegexUtils
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

        savePayment(context, payment)
    }

    private suspend fun savePayment(context: Context, payment: Payment) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            // 중복 데이터 확인
            val existingCount = db.paymentDao().countPaymentByDetails(
                payment.title,
                payment.amount,
                payment.date
            )
            if (existingCount == 0 && payment.amount > 0) {
                // 결제 정보를 데이터베이스에 저장
                db.paymentDao().insert(payment)
                // 알림 표시
                sendNotification(context, payment)
            }
        }
    }

    private fun sendNotification(context: Context, payment: Payment) {
        val title = payment.title
        val detail = if (payment.type == "expense") "지출" else "수입"
        val message = "${detail}: ${formatCurrency(payment.amount)}원"

        NotificationUtils.showNotification(context, title, message)
    }

    suspend fun deletePaymentFromDB(context: Context, id: Long) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
                db.paymentDao().deletePaymentById(id)
        }
    }
}
