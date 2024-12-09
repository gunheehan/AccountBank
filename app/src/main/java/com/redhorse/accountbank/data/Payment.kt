package com.redhorse.accountbank.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)  val id: Long = 0,
    val title: String,
    val type: String, // "income" 또는 "expense"
    val amount: Int,
    val date: String // "YYYY-MM-DD" 형식의 날짜
) : Parcelable {
    fun toPaymentDTO(): PaymentDTO {
        return PaymentDTO(
            id = this.id,
            title = this.title,
            type = this.type,
            amount = this.amount,
            date = this.date
        )
    }
}
