package com.redhorse.accountbank.alarm

import PaymentRepository
import SavePaymentRepository
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class MonthlyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // 데이터 삽입 작업
        CoroutineScope(Dispatchers.IO).launch {
            insertDataToDatabase(context)
        }
    }

    private suspend fun insertDataToDatabase(context: Context) {
        val db = AppDatabaseHelper(context)
        val regularlyDB = SavePaymentRepository(db)
        val regularlyData = regularlyDB.getAllSavePayments()
        if(regularlyData.size == 0)
            return

        val monthDB = PaymentRepository(db)
        for(newpayment in regularlyData)
        {
            val payment = Payment(0,newpayment.title,newpayment.type,newpayment.subtype, newpayment.amount,formatToFullDate(context, newpayment.date.toInt()))
            monthDB.insertOrCreateTableAndInsert(payment)
        }
    }

    fun formatToFullDate(context: Context, day: Int): String {
        // 현재 날짜 가져오기
        val currentDate = LocalDate.now()

        // 현재 년, 월 가져오기
        val currentYearMonth = YearMonth.of(currentDate.year, currentDate.monthValue)

        // 입력된 날짜가 월에 유효한 날짜인지 확인
        if (day < 1 || day > currentYearMonth.lengthOfMonth()) {
            Toast.makeText(context, "유효하지 않은 날짜입니다. $day", Toast.LENGTH_SHORT).show()
            day == 1
        }

        // 완전한 날짜로 변환
        return "${currentDate.year}-${"%02d".format(currentDate.monthValue)}-${"%02d".format(day)}"
    }
}
