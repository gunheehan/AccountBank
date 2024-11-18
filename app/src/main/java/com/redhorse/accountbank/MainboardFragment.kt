package com.redhorse.accountbank

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.redhorse.accountbank.data.AppDatabase
import com.redhorse.accountbank.data.DayData
import com.redhorse.accountbank.data.PaymentDao
import com.redhorse.accountbank.utils.formatCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainboardFragment : Fragment(R.layout.fragment_mainboard) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var paymentDao: PaymentDao
    private lateinit var cardDay: CustomCardView
    private lateinit var cardEarnings: CustomCardView
    private lateinit var cardRemain: CustomCardView
    private var currentMonth: YearMonth = YearMonth.now()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // DB 초기화
        val db = AppDatabase.getDatabase(requireContext())
        paymentDao = db.paymentDao()

        val view = inflater.inflate(R.layout.fragment_mainboard, container, false)

        // CustomCardView 초기화
        cardDay = view.findViewById<CustomCardView>(R.id.card_day)
        cardEarnings = view.findViewById<CustomCardView>(R.id.card_earnings)
        cardRemain = view.findViewById<CustomCardView>(R.id.card_remain)

        // 카드에 내용 추가
        val todayFormatted = getTodayDateFormatted()
        val daysToSalary = calculateDaysToSalary()

        cardDay.addTitle("오늘은 $todayFormatted 입니다.")
        cardDay.addDescription(daysToSalary, Color.RED)

        cardRemain.addTitle("소비 가능 금액 : 950,000 원")
        cardRemain.addSubtitle("아직 여유가 있군요 :)")

        SetMainInfo()

        return view
    }

    private fun SetMainInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val daysInMonth = generateCalendarDataForMonth(currentMonth)
                withContext(Dispatchers.Main) {
                    SetPayData(daysInMonth)
                }
            } catch (e: Exception) {
                Log.e("SetMainInfo", "Error: ${e.message}")
            }
        }
    }

    private fun SetPayData(newDays: List<DayData>) {
        var totalIncome = 0
        var totalExpense = 0

        for (day in newDays) {
            totalIncome += day.getTotalIncome()
            totalExpense += day.getTotalExpense()
        }

        val formattedIncome = formatCurrency(totalIncome) + " 원"
        val formattedExpense = formatCurrency(totalExpense) + " 원"


        if (::cardEarnings.isInitialized) {
            cardEarnings.addTitle("총 수입: $formattedIncome")
            cardEarnings.addTitle("총 지출: $formattedExpense")
            cardEarnings.addTitle("적금 : 2,000,000 원")
        } else {
            Log.e("SetPayData", "cardEarnings is not initialized.")
        }
    }

    private suspend fun generateCalendarDataForMonth(month: YearMonth): List<DayData> {
        val startOfMonth = month.atDay(1)
        val endOfMonth = month.atEndOfMonth()
        val daysList = mutableListOf<DayData>()

        // 첫 날의 요일에 맞춰 빈 칸 추가
        val firstDayOfWeek = startOfMonth.dayOfWeek.value % 7 // 일요일을 0으로 설정
        for (i in 0 until firstDayOfWeek) {
            daysList.add(DayData(LocalDate.MIN)) // 빈 아이템 추가
        }

        // 날짜별로 결제 정보를 추가
        for (day in 1..endOfMonth.dayOfMonth) {
            val date = month.atDay(day)
            val payments = withContext(Dispatchers.IO) {
                paymentDao.getPaymentsForDate(date.toString())
            }

            val dayData = DayData(
                date = date,
                payments = payments?.toMutableList() ?: mutableListOf()
            )

            daysList.add(dayData)
        }

        return daysList
    }

    fun getTodayDateFormatted(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        return today.format(formatter)
    }

    fun calculateDaysToSalary(): String {
        val today = LocalDate.now()
        val salaryDay = today.withDayOfMonth(25) // 이번 달의 25일

        // 월급날이 이미 지난 경우 다음 달 25일로 설정
        val targetDate = if (today.isAfter(salaryDay)) {
            salaryDay.plusMonths(1)
        } else {
            salaryDay
        }

        val daysLeft = ChronoUnit.DAYS.between(today, targetDate)
        return "월급날 D-$daysLeft"
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}