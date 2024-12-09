package com.redhorse.accountbank.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.redhorse.accountbank.data.entity.MonthTable
import com.redhorse.accountbank.data.entity.YearTable

@Dao
interface StaticTableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYear(yearTable: YearTable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonth(monthTable: MonthTable)

    @Query("SELECT * FROM year_table")
    suspend fun getAllYears(): List<YearTable>

    @Query("SELECT * FROM month_table WHERE year = :year")
    suspend fun getMonthsByYear(year: Int): List<MonthTable>

    @Query("SELECT COUNT(*) FROM year_table WHERE year = :year")
    suspend fun yearExists(year: Int): Int

    @Query("SELECT COUNT(*) FROM month_table WHERE year = :year AND month = :month")
    suspend fun monthExists(year: Int, month: Int): Int
}
