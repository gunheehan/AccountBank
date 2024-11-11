package com.redhorse.accountbank.data

import java.time.LocalDate

data class DayData(
    val date: LocalDate = LocalDate.MIN,  // 기본값을 LocalDate.MIN으로 설정
    val income: Int = 0,  // 수입 (기본값 0)
    val expense: Int = 0   // 지출 (기본값 0)
) {
    val isEmpty: Boolean
        get() = date == LocalDate.MIN && income == 0 && expense == 0 // 날짜와 금액 정보로 빈 항목을 판별
}
