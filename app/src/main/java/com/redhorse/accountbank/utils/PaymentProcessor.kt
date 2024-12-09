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

        if (payment.amount < 1)
            return

        // 결제 정보를 동적 테이블에 저장
        savePayment(context, payment)
    }

    private suspend fun savePayment(context: Context, payment: Payment) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)

            // 특정 년도, 월의 테이블 이름 생성 (예: 2024-12)
            val year = LocalDate.now().year
            val month = LocalDate.now().monthValue.toString().padStart(2, '0').toInt()

            // 동적 테이블 생성 여부 확인
            db.dynamicTableDao().createYearMonthTable(payment.date)

            // 중복 데이터 확인
            val existingCount = db.dynamicTableDao().countPayments(
                payment.title,
                payment.amount,
                payment.date
            )

            if (existingCount == 0 && payment.amount > 0) {
                // 결제 정보를 동적 테이블에 저장
                db.dynamicTableDao().insertPayment(payment)
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

    suspend fun deletePaymentFromDB(context: Context, payment: Payment) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)

            // 특정 년도, 월의 테이블 이름 생성 (예: 2024-12)
            val year = LocalDate.now().year
            val month = LocalDate.now().monthValue.toString().padStart(2, '0').toInt()

            // 동적 테이블에서 해당 ID의 결제 삭제
            db.dynamicTableDao().deletePayment(payment.id, payment.date)
        }
    }

}
