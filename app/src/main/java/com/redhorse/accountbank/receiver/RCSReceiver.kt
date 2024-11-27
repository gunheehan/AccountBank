package com.redhorse.accountbank.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.redhorse.accountbank.service.RcsMonitorService
import com.redhorse.accountbank.utils.PaymentProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RCSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val message = extractRcsMessageFromIntent(intent)

        if (message.isNotBlank()) {
            // PaymentProcessor에 메시지 전달
            CoroutineScope(Dispatchers.IO).launch {
                PaymentProcessor.processPayment(context, message)
            }
        }
    }

    private fun extractRcsMessageFromIntent(intent: Intent): String {
        val body = intent.getStringExtra("body") ?: return ""
        // 필요 시 추가 데이터 처리
        return body
    }
}
