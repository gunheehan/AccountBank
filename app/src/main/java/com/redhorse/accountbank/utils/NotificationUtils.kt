package com.redhorse.accountbank.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.redhorse.accountbank.R

object NotificationUtils {
    private const val CHANNEL_ID = "payment_notifications"

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Payment Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_main) // 알림 아이콘
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun updateNotificationChannelImportance(context: Context, enabled: Boolean) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (enabled) NotificationManager.IMPORTANCE_DEFAULT
            else NotificationManager.IMPORTANCE_NONE

            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel != null) {
                channel.importance = importance
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
