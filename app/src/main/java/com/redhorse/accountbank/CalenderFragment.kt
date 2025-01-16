package com.redhorse.accountbank

import com.redhorse.accountbank.modal.DayDetailFragment
import PaymentRepository
import android.content.Context
import com.redhorse.accountbank.adapter.CalendarAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.redhorse.accountbank.data.DayData
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import com.redhorse.accountbank.databinding.ActivityMainBinding
import com.redhorse.accountbank.modal.PaymentEditFragment
import com.redhorse.accountbank.utils.formatCurrency
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.YearMonth

class CalenderFragment : Fragment() {
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var currentDayList : List<DayData>

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var yearMonthTextView: TextView
    private lateinit var prevMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton
    private lateinit var calendarTotalEarningText : TextView
    private lateinit var calendarTotalExpenseText : TextView
    private lateinit var calendarTotalSave : TextView
    private lateinit var payment_insert_Button: ImageButton

    private var currentMonth: YearMonth = YearMonth.now()

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
        setupButtonListeners()
        return view
    }
    override fun onResume() {
        super.onResume()
        updateCalendar()
    }

    private fun showDayDetail(dayData: DayData) {
        val dayDetailFragment = DayDetailFragment.newInstance(
            dayData.date.toString(),
            dayData.payments.map { it.toPaymentDTO() } // 필요한 DTO 형태로 변환하여 전달
        )

        dayDetailFragment.SetOnEditDataCallback { updateCalendar() }
        dayDetailFragment.setOnDetailUpdatedCallback { updatedDate ->
            UpdateDayDetail(updatedDate)
        }
        dayDetailFragment.show(parentFragmentManager, "DayDetailFragment")
    }

    private fun UpdateDayDetail(dayString: String){
        updateCalendar()

        CoroutineScope(Dispatchers.Main).launch {
            val updateDayData = currentDayList.find { it.date.toString() == dayString }

            if (updateDayData != null) {
                delay(500)
                showDayDetail(updateDayData)
            }
        }
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
        calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)
        calendarRecyclerView.adapter = CalendarAdapter(emptyList(), onItemClick = { dayData ->
            showDayDetail(dayData)
        })
    }

    private fun updateCalendar() {
        yearMonthTextView.text = "${currentMonth.year}년 ${currentMonth.monthValue}월"
        CoroutineScope(Dispatchers.IO).launch {
            val daysInMonth = generateCalendarDataForMonth(currentMonth)
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

            editPaymentDialog.show(parentFragmentManager, "PaymentEditFragment")
        }
    }

    private suspend fun generateCalendarDataForMonth(month: YearMonth): List<DayData> {
        val startOfMonth = month.atDay(1)
        val endOfMonth = month.atEndOfMonth()
        val daysList = mutableListOf<DayData>()

        val firstDayOfWeek = startOfMonth.dayOfWeek.value % 7 // 일요일을 0으로 설정
        for (i in 0 until firstDayOfWeek) {
            daysList.add(DayData(LocalDate.MIN)) // 빈 아이템 추가
        }

        val allPaymentsDTO = paymentRepository.getAllPaymentsByMonth(month.toString()) // 비동기적으로 데이터를 가져옵니다.

        for (day in 1.rangeTo(endOfMonth.dayOfMonth)) {
            val date = month.atDay(day)

            val paymentsForDay = allPaymentsDTO.filter { it.date == date.toString() }

            val payments = if (paymentsForDay.isNotEmpty()) {
                paymentsForDay.map { it.toPayment() }.toMutableList()
            } else {
                mutableListOf()
            }

            val dayData = DayData(
                date = date,
                payments = payments
            )

            daysList.add(dayData)
        }

        currentDayList = daysList

        return daysList
    }
}