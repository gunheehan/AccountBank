package com.redhorse.accountbank.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.redhorse.accountbank.utils.NotificationUtils
import com.redhorse.accountbank.utils.PaymentProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RCSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val message = extractRcsMessageFromIntent(intent)

        if (message.isNotBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                PaymentProcessor.processPayment(context, message)
            }
        }
    }

    private fun extractRcsMessageFromIntent(intent: Intent): String {
        val body = intent.getStringExtra("body") ?: return ""
        return body
    }
}