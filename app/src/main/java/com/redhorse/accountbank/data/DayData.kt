package com.redhorse.accountbank.data

import java.time.LocalDate

data class DayData(
    val date: LocalDate,
    val payments: MutableList<Payment> = mutableListOf()
) {
    // 총 수입 계산
    fun getTotalIncome(): Int {
        return payments.filter { it.type == "income" }.sumOf { it.amount }
    }

    // 총 지출 계산
    fun getTotalExpense(): Int {
        return payments.filter { it.type == "expense" }.sumOf { it.amount }
    }
}

