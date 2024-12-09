package com.redhorse.accountbank.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "YearMonthTable")
data class YearMonth(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val month: Int
)
