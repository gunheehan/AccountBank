package com.redhorse.accountbank.data

import java.time.LocalDate

data class DayData(
    val date: LocalDate,  // 날짜 정보
    val income: Int = 0,  // 수입 (기본값 0)
    val expense: Int = 0   // 지출 (기본값 0)
) {
    val isEmpty: Boolean
        get() = income == 0 && expense == 0  // 수입과 지출이 모두 0인 경우
}
