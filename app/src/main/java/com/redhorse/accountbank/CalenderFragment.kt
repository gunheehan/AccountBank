package com.redhorse.accountbank

import com.redhorse.accountbank.modal.DayDetailFragment
import PaymentRepository
import android.content.Context
import com.redhorse.accountbank.adapter.CalendarAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.data.DayData
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import com.redhorse.accountbank.databinding.ActivityMainBinding
import com.redhorse.accountbank.modal.PaymentEditFragment
import com.redhorse.accountbank.utils.formatCurrency
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
 * Use the [CalenderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalenderFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: ActivityMainBinding

    private lateinit var paymentRepository: PaymentRepository

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var yearMonthTextView: TextView
    private lateinit var prevMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton
    private lateinit var calendarTotalEarningText : TextView
    private lateinit var calendarTotalExpenseText : TextView
    private lateinit var calendarTotalSave : TextView
    private lateinit var payment_insert_Button: ImageButton

    private var currentMonth: YearMonth = YearMonth.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dbHelper = AppDatabaseHelper(requireContext())
        paymentRepository = PaymentRepository(dbHelper)
        val view = inflater.inflate(R.layout.fragment_calender, container, false)
        setupViews(view)
        SetCalenterSwipeEvent()
        setupRecyclerView()
        updateCalendar()
        setupButtonListeners()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun showDayDetail(dayData: DayData) {
        // DayData를 전달하여 DayDetailFragment를 띄운다.
        val dayDetailFragment = DayDetailFragment.newInstance(
            dayData.date.toString(),
            dayData.payments.map { it.toPaymentDTO() } // 필요한 DTO 형태로 변환하여 전달
        )

        dayDetailFragment.SetOnEditDataCallback { updateCalendar() }
        dayDetailFragment.show(parentFragmentManager, "DayDetailFragment")
    }

    private fun setupViews(view: View) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        yearMonthTextView = view.findViewById(R.id.yearMonthTextView)
        prevMonthButton = view.findViewById(R.id.prevMonthButton)
        nextMonthButton = view.findViewById(R.id.nextMonthButton)
        calendarTotalEarningText = view.findViewById(R.id.calendarTotalEarning)
        calendarTotalExpenseText = view.findViewById(R.id.calendarTotalExpenses)
        calendarTotalSave = view.findViewById(R.id.calendarTotalSave)
        payment_insert_Button = view.findViewById(R.id.payment_insert_btn)
    }

    private fun SetCalenterSwipeEvent()
    {
        var startX: Float = 0f

        calendarRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = e.x
                    }
                    MotionEvent.ACTION_UP -> {
                        val endX = e.x
                        val diffX = endX - startX

                        if (Math.abs(diffX) > 50) {
                            if (diffX > 0) {
                                currentMonth = currentMonth.minusMonths(1)
                            } else {
                                // 왼쪽 스와이프 -> 다음 달
                                currentMonth = currentMonth.plusMonths(1)
                            }
                            updateCalendar()
                        }
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    private fun setupRecyclerView() {
        calendarRecyclerView.layoutManager = GridLayoutManager(context, 7) // 7일 기준
        calendarRecyclerView.adapter = CalendarAdapter(emptyList(), onItemClick = { dayData ->
            // DayData 클릭 시 실행되는 콜백
            showDayDetail(dayData)
        })
    }

    private fun updateCalendar() {
        yearMonthTextView.text = "${currentMonth.year}년 ${currentMonth.monthValue}월"
        // 비동기로 데이터 생성 후 어댑터에 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            val daysInMonth = generateCalendarDataForMonth(currentMonth)
            // 메인 스레드에서 어댑터 업데이트
            withContext(Dispatchers.Main) {
                (calendarRecyclerView.adapter as CalendarAdapter).updateDays(daysInMonth)
                var totalIncome = 0
                var totalExpense = 0
                var totalSave = 0
                daysInMonth.forEach { dayData ->
                    totalIncome += dayData.getTotalIncome()
                    totalExpense += dayData.getTotalExpense()
                    totalSave += dayData.getTotalSave()
                }

                val formattedIncome = formatCurrency(totalIncome) + " 원"
                val formattedExpense = formatCurrency(totalExpense) + " 원"
                val formattedSave = formatCurrency(totalSave) + " 원"
                calendarTotalEarningText.text = "수입: ${formattedIncome}"
                calendarTotalExpenseText.text = "지출: ${formattedExpense}"
                calendarTotalSave.text = "적금: ${formattedSave}"
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
        payment_insert_Button.setOnClickListener{
            val editPaymentDialog = PaymentEditFragment.newInstance(null)
            editPaymentDialog.setOnSaveCallback { updateCalendar() }

            // 수정 모달 표시
            editPaymentDialog.show(parentFragmentManager, "PaymentEditFragment")
        }
    }

    // DayData를 생성하는 함수에서 변환 사용
    private suspend fun generateCalendarDataForMonth(month: YearMonth): List<DayData> {
        val startOfMonth = month.atDay(1)
        val endOfMonth = month.atEndOfMonth()
        val daysList = mutableListOf<DayData>()

        // 첫 날의 요일에 맞춰 빈 칸 추가
        val firstDayOfWeek = startOfMonth.dayOfWeek.value % 7 // 일요일을 0으로 설정
        for (i in 0 until firstDayOfWeek) {
            daysList.add(DayData(LocalDate.MIN)) // 빈 아이템 추가
        }

        // 해당 월의 모든 결제 정보 비동기적으로 가져오기
        val allPaymentsDTO = paymentRepository.getAllPaymentsByMonth(month.toString()) // 비동기적으로 데이터를 가져옵니다.

        // 날짜별로 결제 정보를 추가
        for (day in 1.rangeTo(endOfMonth.dayOfMonth)) {
            val date = month.atDay(day)

            // 해당 날짜에 맞는 결제 정보만 필터링
            val paymentsForDay = allPaymentsDTO.filter { it.date == date.toString() }

            // 날짜에 관계없이 항상 DayData 추가 (결제 데이터가 없더라도)
            val payments = if (paymentsForDay.isNotEmpty()) {
                paymentsForDay.map { it.toPayment() }.toMutableList()
            } else {
                mutableListOf()  // 결제 데이터가 없으면 빈 리스트로 설정
            }

            val dayData = DayData(
                date = date,
                payments = payments
            )

            daysList.add(dayData)
        }

        return daysList
    }

    private suspend fun savePaymentAndNotify(context: Context, payment: Payment) {
        paymentRepository.insertOrCreateTableAndInsert(payment)
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
            CalenderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}