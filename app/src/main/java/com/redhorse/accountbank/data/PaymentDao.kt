//package com.redhorse.accountbank.data
//
//import MonthTable
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.Query
//import com.redhorse.accountbank.data.entity.YearTable
//
//@Dao
//interface PaymentDao {
//    @Insert
//    suspend fun insert(payment: Payment)
//
//    @Query("SELECT * FROM payments WHERE date BETWEEN :startOfMonth AND :endOfMonth")
//    suspend fun getPaymentsForMonth(startOfMonth: String, endOfMonth: String): List<Payment>
//
//    @Query("SELECT COUNT(*) FROM Payments WHERE title = :title AND amount = :amount AND date = :date")
//    suspend fun countPaymentByDetails(title: String, amount: Int, date: String): Int
//
//    @Query("SELECT * FROM payments WHERE date = :date")
//    fun getPaymentsForDate(date: String): List<Payment>?
//
//    @Query("UPDATE payments SET title = :title, amount = :amount, date = :date, type = :type WHERE id = :id")
//    suspend fun updatePaymentById(id: Long, title: String, amount: Int, date: String, type: String)
//
//    @Query("DELETE FROM payments WHERE id = :id")
//    suspend fun deletePaymentById(id: Long)
//}
//
//@Dao
//interface StaticTableDao {
//    @Insert
//    suspend fun insertYear(yearTable: YearTable)
//
//    @Insert
//    suspend fun insertMonth(monthTable: MonthTable)
//
//    @Query("SELECT * FROM year_table")
//    suspend fun getAllYears(): List<YearTable>
//
//    @Query("SELECT * FROM month_table WHERE year = :year")
//    suspend fun getMonthsByYear(year: Int): List<MonthTable>
//}
//
//@Dao
//interface DynamicTableDao {
//    @Query("CREATE TABLE IF NOT EXISTS payment_:tableName (id INTEGER PRIMARY KEY, title TEXT, type TEXT, amount INTEGER, date TEXT)")
//    suspend fun createTable(tableName: String)
//
//    @Query("INSERT INTO payment_:tableName (title, type, amount, date) VALUES (:title, :type, :amount, :date)")
//    suspend fun insertIntoTable(
//        tableName: String,
//        title: String,
//        type: String,
//        amount: Int,
//        date: String
//    )
//
//    @Query("SELECT * FROM :tableName WHERE date = :date")
//    suspend fun getPaymentsFromTable(tableName: String, date: String): List<Payment>
//}
//
//
