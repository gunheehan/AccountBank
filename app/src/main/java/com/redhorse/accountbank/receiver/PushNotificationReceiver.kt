package com.redhorse.accountbank.receiver

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.redhorse.accountbank.utils.NotificationUtils
import com.redhorse.accountbank.utils.PaymentProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiverService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: "알림"
        val message = extras.getString(Notification.EXTRA_TEXT) ?: "메시지 내용 없음"

        NotificationUtils.showNotification(applicationContext, title, message)

        // PaymentProcessor에 메시지 전달
        CoroutineScope(Dispatchers.IO).launch {
            PaymentProcessor.processPayment(applicationContext, message)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 알림이 삭제될 때 필요한 동작이 있으면 구현
    }
}

