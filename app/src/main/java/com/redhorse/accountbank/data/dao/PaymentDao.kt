package com.redhorse.accountbank.data.dao

import androidx.room.*
import com.redhorse.accountbank.data.Payment

@Dao
interface PaymentDao {
    @Insert
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT * FROM payment_table WHERE date = :date")
    suspend fun getPaymentsByDate(date: String): List<Payment>
}
