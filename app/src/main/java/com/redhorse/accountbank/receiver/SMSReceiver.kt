package com.redhorse.accountbank.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import com.redhorse.accountbank.utils.PaymentProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val message = extractMessageFromIntent(intent)

        if (message.isNotBlank()) {
            // 결제 메시지 처리
            CoroutineScope(Dispatchers.IO).launch {
                PaymentProcessor.processPayment(context, message)
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
}
