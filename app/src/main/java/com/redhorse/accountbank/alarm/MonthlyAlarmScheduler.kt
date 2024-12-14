package com.redhorse.accountbank.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

class MonthlyAlarmScheduler {
    fun scheduleMonthlyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MonthlyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 매달 1일 00:00에 알람 설정
        val firstDayOfNextMonth = calculateFirstDayOfNextMonth()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            firstDayOfNextMonth,
            pendingIntent
        )
    }

    private fun calculateFirstDayOfNextMonth(): Long {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1) // 다음 달로 이동
            set(Calendar.DAY_OF_MONTH, 1) // 1일로 설정
            set(Calendar.HOUR_OF_DAY, 0) // 00시
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
