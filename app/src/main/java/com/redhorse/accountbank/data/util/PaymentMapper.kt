package com.redhorse.accountbank.data.utils

import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.PaymentDTO

object PaymentMapper {
    fun toPaymentList(paymentDTOs: List<PaymentDTO>): List<Payment> {
        return paymentDTOs.map { it.toPayment() }
    }

    fun toPaymentDTOList(payments: List<Payment>): List<PaymentDTO> {
        return payments.map { it.toPaymentDTO() }
    }
}
