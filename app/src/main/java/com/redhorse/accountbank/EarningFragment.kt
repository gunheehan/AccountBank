package com.redhorse.accountbank

import android.content.Context
import com.redhorse.accountbank.adapter.CalendarAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.data.AppDatabase
import com.redhorse.accountbank.data.DayData
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.PaymentDao
import com.redhorse.accountbank.utils.formatCurrency
import com.redhorse.accountbank.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EarningFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EarningFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var paymentDao: PaymentDao

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var yearMonthTextView: TextView
    private lateinit var prevMonthButton: Button
    private lateinit var nextMonthButton: Button

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
        // 데이터베이스 초기화 및 DAO 가져오기
        val db = AppDatabase.getDatabase(requireContext())
        paymentDao = db.paymentDao()

        val view = inflater.inflate(R.layout.fragment_earning, container, false)
        setupViews(view)
        setupRecyclerView()
        updateCalendar()
        setupButtonListeners()
        return view
    }

    private fun setupViews(view: View) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        yearMonthTextView = view.findViewById(R.id.yearMonthTextView)
        prevMonthButton = view.findViewById(R.id.prevMonthButton)
        nextMonthButton = view.findViewById(R.id.nextMonthButton)
    }

    private fun setupRecyclerView() {
        calendarRecyclerView.layoutManager = GridLayoutManager(context, 7) // 7일 기준
        calendarRecyclerView.adapter = CalendarAdapter(emptyList()) // 초기 어댑터 설정
    }

    private fun updateCalendar() {
        yearMonthTextView.text = "${currentMonth.year}년 ${currentMonth.monthValue}월"
        val daysInMonth = generateCalendarDataForMonth(currentMonth)
        Log.d("EarningFragment", "daysInMonth: $daysInMonth")
        // 비동기로 데이터 생성 후 어댑터에 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            val daysInMonth = generateCalendarDataForMonth(currentMonth)

            // 메인 스레드에서 어댑터 업데이트
            withContext(Dispatchers.Main) {
                (calendarRecyclerView.adapter as CalendarAdapter).updateDays(daysInMonth)
            }
        }
    }



    private fun setupButtonListeners() {
        prevMonthButton.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateCalendar()
        }
        nextMonthButton.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateCalendar()
        }
    }

    private fun generateCalendarDataForMonth(month: YearMonth): List<DayData> {
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
            val income = paymentDao.getIncomeForDate(date.toString())
            val expense = paymentDao.getExpenseForDate(date.toString())

            // 결제 정보가 없을 경우 0으로 초기화
            daysList.add(DayData(date, income = income ?: 0, expense = expense ?: 0))
        }

        return daysList
    }


    fun processPaymentMessage(context: Context, message: String) {
        val payment = parsePaymentInfo(message) // 메시지 파싱
        CoroutineScope(Dispatchers.IO).launch {
            savePaymentAndNotify(context, payment) // DB에 저장하고 알림 표시
        }
    }

    private fun parsePaymentInfo(message: String): Payment {
        // 메시지를 파싱하여 Payment 객체를 생성하는 로직을 구현하세요
        // 예: 제목, 타입, 금액 등을 추출하여 Payment 객체 반환
        return Payment(title = "예제 제목", type = "income", amount = 10000, date = "2024-11-04")
    }

    private suspend fun savePaymentAndNotify(context: Context, payment: Payment) {
        val db = AppDatabase.getDatabase(context)
        db.paymentDao().insert(payment)

        val title = payment.title
        val message = "결제 금액: ${formatCurrency(payment.amount)}원, 타입: ${payment.type}"
        NotificationUtils.showNotification(context, title, message)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EarningFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EarningFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}