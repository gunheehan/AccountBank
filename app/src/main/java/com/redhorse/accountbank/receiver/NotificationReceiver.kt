package com.redhorse.accountbank.receiver

import android.app.Notification
import android.content.Context
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.utils.NotificationUtils
import com.redhorse.accountbank.utils.PaymentProcessor
import com.redhorse.accountbank.utils.formatCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NotificationReceiver : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        if (packageName == applicationContext.packageName) {
            return
        }

        val extras = sbn.notification.extras
        val message = extras.getString(Notification.EXTRA_TEXT) ?: "메시지 내용 없음"

        if (isPaymentRelated(message)) {
            CoroutineScope(Dispatchers.IO).launch {
                PaymentProcessor.processPayment(applicationContext, message)
            }
        }
    }

    private fun isPaymentRelated(message: String): Boolean {
        val regex = Regex("결제|입금|출금|송금|승인|일시불")
        return regex.containsMatchIn(message)
    }
}
