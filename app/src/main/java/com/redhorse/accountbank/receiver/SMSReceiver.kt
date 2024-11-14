package com.redhorse.accountbank.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import com.redhorse.accountbank.data.AppDatabase
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.utils.NotificationUtils
import com.redhorse.accountbank.utils.formatCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val message = extractMessageFromIntent(intent)

        if (message.isNotBlank()) {
            // 정규식으로 결제 정보 추출
            val payment = RegexUtils.parsePaymentInfo(message, LocalDate.now().toString())
            // DB에 저장 및 알림 발송
            CoroutineScope(Dispatchers.IO).launch {
                savePaymentAndNotify(context, payment)
            }
        }
    }

    private fun extractMessageFromIntent(intent: Intent): String {
        // 메시지 추출 로직 (예시: SMS 메시지 내용)
        val bundle = intent.extras
        val smsMessage = bundle?.get("pdus") as Array<*>
        val messages = smsMessage.map {
            val format = bundle.getString("format")
            SmsMessage.createFromPdu(it as ByteArray, format).messageBody
        }
        return messages.joinToString(" ")
    }

    private suspend fun savePaymentAndNotify(context: Context, payment: Payment) {
        // DB에 결제 정보 저장
        val db = AppDatabase.getDatabase(context)
        db.paymentDao().insert(payment)

        // 알림 표시
        val title = payment.title
        val message = "결제 금액: ${formatCurrency(payment.amount)}원, 타입: ${payment.type}"
        NotificationUtils.showNotification(context, title, message)
    }
}
