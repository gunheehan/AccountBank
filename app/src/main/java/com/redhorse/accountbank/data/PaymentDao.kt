package com.redhorse.accountbank.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment)

    @Query("SELECT * FROM payments WHERE date BETWEEN :startOfMonth AND :endOfMonth")
    suspend fun getPaymentsForMonth(startOfMonth: String, endOfMonth: String): List<Payment>

    @Query("SELECT COUNT(*) FROM Payments WHERE title = :title AND amount = :amount AND date = :date")
    suspend fun countPaymentByDetails(title: String, amount: Int, date: String): Int

    @Query("SELECT * FROM payments WHERE date = :date")
    fun getPaymentsForDate(date: String): List<Payment>?

    @Query("UPDATE payments SET title = :title, amount = :amount, date = :date, type = :type WHERE id = :id")
    suspend fun updatePaymentById(id: Long, title: String, amount: Int, date: String, type: String)

}

