package com.redhorse.accountbank.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

class MonthlyAlarmScheduler {
    fun scheduleMonthlyAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MonthlyAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 매달 1일 00:00 타임스탬프 계산
            val firstDayOfNextMonth = calculateFirstDayOfNextMonth()

            // firstDayOfNextMonth 값 검증
            if (firstDayOfNextMonth <= 0) {
                return
            }

            // AlarmManager에 알람 설정
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                firstDayOfNextMonth,
                pendingIntent
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun calculateFirstDayOfNextMonth(): Long {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 1) // 다음 달로 이동
            calendar.set(Calendar.DAY_OF_MONTH, 1) // 1일로 설정
            calendar.set(Calendar.HOUR_OF_DAY, 0) // 자정으로 설정
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            calendar.timeInMillis // 타임스탬프 반환
        } catch (e: Exception) {
            -1 // 유효하지 않은 값 반환
        }
    }

}
