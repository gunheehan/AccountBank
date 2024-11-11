package com.redhorse.accountbank.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentRepository(context: Context) {
    private val paymentDao = AppDatabase.getDatabase(context).paymentDao()

    fun savePayment(payment: Payment) {
        CoroutineScope(Dispatchers.IO).launch {
            paymentDao.insert(payment)
        }
    }
}
