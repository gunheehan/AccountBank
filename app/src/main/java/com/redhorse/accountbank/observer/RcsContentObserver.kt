package com.redhorse.accountbank.observer

import PaymentRepository
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class RcsContentObserver(
    private val context: Context,
    handler: Handler
) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.d("RcsContentObserver", "데이터 변경 감지: $uri")
        uri?.let {
            // RCS 메시지 읽기 및 처리
            CoroutineScope(Dispatchers.IO).launch {
                processNewRcsMessage(context, it)
            }
        }
    }

    private suspend fun processNewRcsMessage(context: Context, uri: Uri) {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (cursor.moveToFirst()) {
                val messageColumn = cursor.getColumnIndex("body")
                val message = cursor.getString(messageColumn)
                val payment = RegexUtils.parsePaymentInfo(message, LocalDate.now().toString())

                // 결제 정보를 데이터베이스에 저장
                val dbHelper = AppDatabaseHelper(context)
                val paymentRepository = PaymentRepository(dbHelper)
                paymentRepository.insertOrCreateTableAndInsert(payment)

                Log.d("RcsContentObserver", "결제 정보 저장 완료: $payment")
            }
        }
    }
}
