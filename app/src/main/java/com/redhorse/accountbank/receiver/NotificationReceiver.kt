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

class NotificationReceiver : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        val packageName = sbn.packageName

        // 알림에서 메시지 추출
        val message = extractMessageFromNotification(extras)
        sendNotification(applicationContext, message)

        if (message.isNotBlank()) {
            // PaymentProcessor에 메시지 전달
            CoroutineScope(Dispatchers.IO).launch {
                PaymentProcessor.processPayment(applicationContext, message)
            }
        }

        Log.d("NotificationReceiver", "Notification received from $packageName: $message")
    }

    private fun extractMessageFromNotification(extras: Bundle): String {
        // 알림의 내용 추출 (예: 제목과 텍스트)
        val title = extras.getString(Notification.EXTRA_TITLE, "")
        val text = extras.getString(Notification.EXTRA_TEXT, "")

        // 필요시 특정 형태의 데이터를 가공하여 반환
        return "$title $text".trim()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotificationReceiver", "Notification removed: ${sbn.packageName}")
    }

    private fun sendNotification(context: Context, msg: String) {
        val title = "Notification Receiver"
        val message = msg
        NotificationUtils.showNotification(context, title, message)
    }
}
