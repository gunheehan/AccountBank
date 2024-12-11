package com.redhorse.accountbank.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redhorse.accountbank.utils.toPayment
import kotlinx.parcelize.Parcelize

@Entity(tableName = "payments")
@Parcelize
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "income" 또는 "expense"
    val amount: Int,
    val date: String // "YYYY-MM-DD" 형식의 날짜
) : Parcelable {
    fun toPayment(): Payment {
        return Payment(
            id = this.id,
            title = this.title,
            type = this.type,
            amount = this.amount,
            date = this.date
        )
    }
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
