package com.redhorse.accountbank.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dynamic_payment_table") // 기본 모델
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String,
    val amount: Int,
    val date: String // YYYY-MM-DD 형식
)
