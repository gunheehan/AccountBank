package com.redhorse.accountbank.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "income" 또는 "expense"
    val amount: Int,
    val date: String // "YYYY-MM-DD" 형식의 날짜
)
