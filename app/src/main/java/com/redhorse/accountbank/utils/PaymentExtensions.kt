package com.redhorse.accountbank.utils

import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.PaymentDTO

fun PaymentDTO.toPayment(): Payment {
    return Payment(
        id = this.id,
        title = this.title,
        type = this.type,
        subtype = this.subtype,
        amount = this.amount,
        date = this.date
    )
}
