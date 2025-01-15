package com.redhorse.accountbank

import PaymentRepository
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.redhorse.accountbank.data.AppInfo
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import com.redhorse.accountbank.data.helper.AppinfoHelper
import com.redhorse.accountbank.item.CustomCardView
import com.redhorse.accountbank.modal.MainInfoModal
import com.redhorse.accountbank.utils.formatCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ReportFragment : Fragment(R.layout.fragment_report) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var paymentRepository: PaymentRepository
    private lateinit var cardDay: CustomCardView
    private lateinit var cardEarnings: CustomCardView
    private lateinit var cardRemain: CustomCardView
    private lateinit var cardPaymentRatio: CustomCardView
    private lateinit var currentDayText: TextView
    private lateinit var premonthButton: ImageButton
    private lateinit var nextmonthButton: ImageButton
    private lateinit var fixedButton: ImageButton
    private lateinit var appinfoHelper: AppinfoHelper


    private var currentYear: Int = 0
    private var currentMonth: Int = 0
    private var totalIncome: Int = 0
    private var totalExpense: Int = 0
    private var totalSave: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dbHelper = AppDatabaseHelper(requireContext())
        paymentRepository = PaymentRepository(dbHelper)
        appinfoHelper = AppinfoHelper(requireContext())

        val view = inflater.inflate(R.layout.fragment_report, container, false)

        cardDay = view.findViewById(R.id.card_day)
        cardEarnings = view.findViewById(R.id.card_earnings)
        cardRemain = view.findViewById(R.id.card_remain)
        cardPaymentRatio = view.findViewById(R.id.card_payment_ratio)
        currentDayText = view.findViewById(R.id.mainboard_day_text)
        premonthButton = view.findViewById(R.id.mainboard_day_leftButton)
        nextmonthButton = view.findViewById(R.id.mainboard_day_rightButton)
        fixedButton = view.findViewById(R.id.fixed_button)

        premonthButton.setOnClickListener(){
            onPreviousMonthButtonClicked()
        }

        nextmonthButton.setOnClickListener(){
            onNextMonthButtonClicked()
        }

        fixedButton.setOnClickListener() {
            showMainInfoModal()
        }

        initializeDate()

        return view
    }

    override fun onResume() {
        super.onResume()
        SetMainCard()
        SetMainInfo()
        updateDateUI()
    }

    fun initializeDate() {
        val currentDate = LocalDate.now()
        currentYear = currentDate.year
        currentMonth = currentDate.monthValue
    }

    fun updateDateUI() {
        val formattedDate = "${currentYear}년 ${currentMonth}월"
        currentDayText.text = formattedDate // TextView에 날짜 표시
    }

    fun onPreviousMonthButtonClicked() {
        if (currentMonth == 1) {
            currentMonth = 12
            currentYear -= 1
        } else {
            currentMonth -= 1
        }
        updateDateUI()
        UpdateMainInfo()
        SetMainInfo()
    }

    fun onNextMonthButtonClicked() {
        if (currentMonth == 12) {
            currentMonth = 1
            currentYear += 1
        } else {
            currentMonth += 1
        }
        updateDateUI()
        UpdateMainInfo()
        SetMainInfo()
    }

    private fun showMainInfoModal() {
        val mainInfoModal = MainInfoModal.newInstance(appinfoHelper) {
            UpdateMainInfo()
        }
        mainInfoModal.show(childFragmentManager, "MainInfoModal")
    }

    private fun UpdateMainInfo() {
        SetMainCard()
        SetRemainCard()
    }

    private fun SetMainCard() {
        cardDay.clear()

        val todayFormatted = getTodayDateFormatted()
        cardDay.addTitle("오늘은 $todayFormatted 입니다.")

        val dday_title = appinfoHelper.getString(AppInfo.STRING_D_DAY_TITLE, "")
        val dday = appinfoHelper.getInt(AppInfo.INT_D_DAY_DAY, 0)

        if (dday_title.isNotEmpty() && dday != 0) {
            val daysToSalary = calculateDaysToSalary(dday)
            cardDay.addDescription("$dday_title - $daysToSalary 일", Color.DKGRAY)
        }
    }

    private fun SetRemainCard() {
        cardRemain.clear()

        val targetAmount = appinfoHelper.getInt(AppInfo.INT_EXPENSE_TARGET, 0)

        if (targetAmount == 0) {
            cardRemain.addTitle("소비 설정 금액 : 0 원")
            cardRemain.addSubtitle("하단 버튼으로 데이터를 설정 후 사용해주세요")
        } else {
            cardRemain.addTitle("소비 설정 금액 : ${formatCurrency(targetAmount)} 원")
            cardRemain.addSubtitle("소비 가능 금액 : ${formatCurrency(targetAmount - totalExpense)} 원")
        }
    }

    private fun SetMainInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val daysInMonth = generateCalendarDataForMonth()
                withContext(Dispatchers.Main) {
                    SetPayData(daysInMonth)
                    SetPaymentRatio(daysInMonth)
                }
            } catch (e: Exception) {
                Log.e("SetMainInfo", "Error: ${e.message}")
            }
        }
    }

    private fun SetPayData(newDays: List<Payment>) {
        cardEarnings.Container.removeAllViews()
        totalIncome = 0
        totalExpense = 0
        totalSave = 0

        for (day in newDays) {
            if(day.type.equals("income")) {
                totalIncome += day.amount
            }
            else if(day.type.equals("expense")){
                totalExpense += day.amount
            }
            else{
                totalSave += day.amount
            }
        }

        val formattedIncome = formatCurrency(totalIncome) + " 원"
        val formattedExpense = formatCurrency(totalExpense) + " 원"
        val formattedSave = formatCurrency(totalSave) + " 원"


        if (::cardEarnings.isInitialized) {
            cardEarnings.addTitle("총 수입: $formattedIncome")
            cardEarnings.addTitle("총 지출: $formattedExpense")
            cardEarnings.addTitle("적금 : $formattedSave")

            SetRemainCard()
        } else {
            Log.e("SetPayData", "cardEarnings is not initialized.")
        }
    }

    private fun SetPaymentRatio(newDays: List<Payment>)
    {
        cardPaymentRatio.Container.removeAllViews()

        cardPaymentRatio.addTitle("소비 분포도")

        var totalFood = 0
        var totalSnack = 0
        var totalTransfort = 0
        var totalLife = 0
        var totalOther = 0

        for(payment in newDays)
        {
            if(!payment.type.equals("expense"))
                continue;

            when(payment.subtype){
                0->totalFood += payment.amount
                1->totalSnack += payment.amount
                2->totalTransfort += payment.amount
                3->totalLife += payment.amount
                4->totalOther += payment.amount
            }
        }
        cardPaymentRatio.addSubtitle("식비 : ${formatCurrency(totalFood)}원", "${calculatePercentage(totalFood)}%")
        cardPaymentRatio.addSubtitle("간식 : ${formatCurrency(totalSnack)}원", "${calculatePercentage(totalSnack)}%")
        cardPaymentRatio.addSubtitle("여가 : ${formatCurrency(totalLife)}원", "${calculatePercentage(totalLife)}%")
        cardPaymentRatio.addSubtitle("교통 : ${formatCurrency(totalTransfort)}원", "${calculatePercentage(totalTransfort)}%")
        cardPaymentRatio.addSubtitle("기타 : ${formatCurrency(totalOther)}원", "${calculatePercentage(totalOther)}%")
    }

    private suspend fun generateCalendarDataForMonth(): List<Payment> {
        val payments = withContext(Dispatchers.IO) {
            val dayformat = "$currentYear-$currentMonth-1"
            paymentRepository.getAllPaymentsByMonth(dayformat)
        }

        return payments
    }

    fun getTodayDateFormatted(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        return today.format(formatter)
    }

    fun calculateDaysToSalary(day: Int): String {
        var errormsg = "";
        val today = LocalDate.now()
        val salaryDay: LocalDate = try {
            today.withDayOfMonth(day)
        } catch (e: DateTimeException) {
            errormsg = " 설정일 확인이 필요합니다.(현재 설정일 ${day})"
            today.withDayOfMonth(1)
        }

        val targetDate = if (today.isAfter(salaryDay)) {
            salaryDay.plusMonths(1)
        } else {
            salaryDay
        }

        val daysLeft = ChronoUnit.DAYS.between(today, targetDate)
        return daysLeft.toString() + errormsg
    }


    private fun calculatePercentage(expenseTotla: Int): Int {
        return if (totalExpense == 0) {
            0
        } else {
            val percentage = (expenseTotla * 100) / totalExpense
            percentage
        }
    }
}