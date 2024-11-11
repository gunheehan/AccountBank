package com.redhorse.accountbank.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.redhorse.accountbank.utils.NotificationUtils

class PushNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 푸시 알림을 처리하는 로직
        val title = intent.getStringExtra("title") ?: "알림"
        val message = intent.getStringExtra("message") ?: "메시지 내용 없음"

        // NotificationUtils를 사용하여 알림 표시
        NotificationUtils.showNotification(context, title, message)
    }
}
