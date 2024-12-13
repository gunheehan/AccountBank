package com.redhorse.accountbank.utils

import PaymentRepository
import RegexUtils
import android.content.Context
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
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
        if(payment.amount <= 0)
            return;
        withContext(Dispatchers.IO) {

            val dbHelper = AppDatabaseHelper(context)
            val paymentRepository = PaymentRepository(dbHelper)
            paymentRepository.insertOrCreateTableAndInsert(payment)

            sendNotification(context, payment)
        }
    }

    private fun sendNotification(context: Context, payment: Payment) {
        val title = payment.title
        val detail = if (payment.type == "expense") "지출" else if(payment.type == "income") "수입" else "적금"
        val message = "${detail}: ${formatCurrency(payment.amount)}원"

        NotificationUtils.showNotification(context, title, message)
    }

    suspend fun deletePaymentFromDB(context: Context, date: String, id: Long) {
        withContext(Dispatchers.IO) {
            val dbHelper = AppDatabaseHelper(context)
            val paymentRepository = PaymentRepository(dbHelper)
            paymentRepository.deletePaymentById(date, id)
        }
    }
}
