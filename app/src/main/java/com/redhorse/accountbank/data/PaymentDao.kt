package com.redhorse.accountbank.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PaymentDao {
    @Insert
    suspend fun insertPayment(payment: Payment)

    @Query("SELECT * FROM payments WHERE date BETWEEN :startOfMonth AND :endOfMonth")
    fun getPaymentsForMonth(startOfMonth: String, endOfMonth: String): List<Payment>
}
