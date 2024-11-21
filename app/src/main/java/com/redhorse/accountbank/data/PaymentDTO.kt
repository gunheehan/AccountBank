package com.redhorse.accountbank.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentDTO(
    val id: Long,
    val title: String,
    val type: String, // "income" 또는 "expense"
    val amount: Int,
    val date: String
) : Parcelable
