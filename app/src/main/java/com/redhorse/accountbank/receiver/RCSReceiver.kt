package com.redhorse.accountbank.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.redhorse.accountbank.data.AppDatabase
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.utils.NotificationUtils
import com.redhorse.accountbank.utils.formatCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class RCSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            Log.e("RCSReceiver", "Context or Intent is null.")
            return
        }

        // 디버깅: Intent Extras 확인
        val bundle = intent.extras
        bundle?.keySet()?.forEach { key ->
            Log.d("RCSReceiver", "Key: $key, Value: ${bundle.get(key)}")
        }

        val message = extractMessageFromIntent(intent)
        if (message.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val payment = RegexUtils.parsePaymentInfo(message, LocalDate.now().toString())
                    savePaymentAndNotify(context, payment)
                } catch (e: Exception) {
                    Log.e("RCSReceiver", "Error processing payment: ${e.message}")
                }
            }
        } else {
            Log.d("RCSReceiver", "No valid message found.")
        }
    }

    private fun extractMessageFromIntent(intent: Intent): String {
        val bundle = intent.extras
        if (bundle == null) {
            Log.e("RCSReceiver", "No extras in intent.")
            return ""
        }

        // RCS 메시지 확인 (예: Samsung RCS 메시지 데이터 구조)
        return bundle.getString("rcs_message_body") ?: "Unknown message format"
    }

    private suspend fun savePaymentAndNotify(context: Context, payment: Payment) {
        val db = AppDatabase.getDatabase(context)
        val existingCount = db.paymentDao().countPaymentByDetails(payment.title, payment.amount, payment.date)

        if (existingCount == 0) {
            db.paymentDao().insert(payment)
            val title = payment.title
            val message = "결제 금액: ${formatCurrency(payment.amount)}원, 타입: ${payment.type}"
            NotificationUtils.showNotification(context, title, message)
        } else {
            Log.d("RCSReceiver", "Duplicate payment skipped.")
        }
    }
}
