package com.redhorse.accountbank.modal

import PaymentRepository
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.redhorse.accountbank.R
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.helper.AppDatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import com.redhorse.accountbank.adapter.CustomSpinnerAdapter


class PaymentEditFragment : DialogFragment() {

    private lateinit var paymentRepository: PaymentRepository

    private lateinit var onSaveCallback: () -> Unit
    private lateinit var payment_title_EditText : EditText
    private lateinit var select_day_TextView : TextView
    private lateinit var select_day_Button : Button
    private lateinit var amount_EditText : EditText
    private lateinit var payment_type_Spinner : Spinner
    private lateinit var payment_subtype_Spinner : Spinner
    private lateinit var insert_Button : Button

    private var isInsertMode : Boolean = false
    private var paymentData: Payment? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dbHelper = AppDatabaseHelper(requireContext())
        paymentRepository = PaymentRepository(dbHelper)

        arguments?.let {
            paymentData = it.getParcelable("payment") // 전달된 Payment 객체 받기
        }

        // Fragment의 레이아웃을 인플레이트합니다.
        val rootView = inflater.inflate(R.layout.fragment_payment_input, container, false)

        payment_title_EditText = rootView.findViewById(R.id.payment_title_edit)
        select_day_TextView = rootView.findViewById(R.id.payment_select_day_text)
        amount_EditText = rootView.findViewById(R.id.payment_amount_edit)
        payment_type_Spinner = rootView.findViewById(R.id.payment_type_spinner)
        insert_Button = rootView.findViewById(R.id.payment_insert_btn)
        payment_subtype_Spinner = rootView.findViewById(R.id.payment_subtype_spinner)


        val paymentTypes = resources.getStringArray(R.array.payment_types)
        val paymentTypeList = paymentTypes.toList()
        val adapter = CustomSpinnerAdapter(requireContext(), paymentTypeList)
        payment_type_Spinner.adapter = adapter

        payment_type_Spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                updateSubtypeSpinner(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때의 처리
            }
        })

        // 날짜 버튼 초기화 추가
        select_day_Button = rootView.findViewById(R.id.payment_select_day_btn)

        if(paymentData == null){
            isInsertMode = true
        }

        // UI에 데이터를 설정하는 로직
        setupUI(rootView)

        insert_Button.setOnClickListener {
            if (isInsertMode) {
                insertPayment()
            } else {
                updatePayment()
            }
        }

        select_day_Button.setOnClickListener {
            openDatePickerDialog()
        }

        return rootView
    }

    fun updateSubtypeSpinner(paymentTypePosition: Int) {
        var paymentTypes = resources.getStringArray(R.array.income_subtypes).toList()

        when(paymentTypePosition){
            0-> paymentTypes = resources.getStringArray(R.array.income_subtypes).toList()
            1-> paymentTypes = resources.getStringArray(R.array.expense_subtypes).toList()
            2-> paymentTypes = resources.getStringArray(R.array.save_subtypes).toList()
        }

        // payment_subtype_Spinner에 새롭게 선택된 subtype 리스트 설정
        val subtypeAdapter = CustomSpinnerAdapter(requireContext(), paymentTypes)
        payment_subtype_Spinner.adapter = subtypeAdapter

        // 기본값은 항상 "기타"로 설정
        payment_subtype_Spinner.setSelection(paymentTypes.indexOf("기타"))
    }

    private fun setupUI(rootView: View) {
        paymentData?.let {
            // 내역 표시
            payment_title_EditText.setText(it.title)
            if (isInsertMode)
            {
                // 오늘 날짜 가져오기
                val today = LocalDate.now()
                val formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy-dd-MM"))

                select_day_TextView.text = formattedDate
            }
            else
                select_day_TextView.setText(it.date)
            amount_EditText.setText(it.amount.toString())
            val isIncome = if (it.type == "지출") "expense" else if(it.type == "수입") "income" else "save"

            if (it.type.equals("income")){
                payment_type_Spinner.setSelection(0)
                updateSubtypeSpinner(0)
            }
            else if(it.type.equals("expense")){
                payment_type_Spinner.setSelection(1)
                updateSubtypeSpinner(1)
            }
            else{
            payment_type_Spinner.setSelection(2)
            updateSubtypeSpinner(2)
        }

        } ?: run {
        }
    }

    fun setOnSaveCallback(callback: () -> Unit) {
        this.onSaveCallback = callback
    }

    companion object {
        // 새로운 인스턴스를 만들 때 Payment 객체가 null일 수 있으므로 null 처리 추가
        fun newInstance(payment: Payment?): PaymentEditFragment {
            val fragment = PaymentEditFragment()
            val bundle = Bundle()
            payment?.let {
                bundle.putParcelable("payment", it) // Payment 객체가 null이 아니면 전달
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun openDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // DatePickerDialog 생성
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // 선택된 날짜를 yyyy-MM-dd 형식으로 TextView에 설정
                val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                select_day_TextView.text = selectedDate
            },
            year, month, dayOfMonth
        )

        // 날짜 선택 다이얼로그 표시
        datePickerDialog.show()
    }

    fun insertPayment() {
        val titleData = payment_title_EditText.text.toString()
        val date = select_day_TextView.text.toString()
        val amount = amount_EditText.text.toString().toIntOrNull() ?: 0
        var type = payment_type_Spinner.selectedItem.toString()
        type = if (type == "수입") "income" else if(type == "지출") "expense" else "save"
        var subtype = payment_subtype_Spinner.selectedItemPosition

        // 새 Payment 객체 생성
        val newPayment = Payment(
            id = 0, // Room에서 자동 생성되므로 0으로 설정
            title = titleData,
            type = type,
            subtype = subtype,
            amount = amount,
            date = date
        )

        CoroutineScope(Dispatchers.IO).launch {
            paymentRepository.insertOrCreateTableAndInsert(newPayment)

            // UI 업데이트를 위한 콜백 호출
            withContext(Dispatchers.Main) {
                dismiss() // 다이얼로그 닫기

                onSaveCallback!!.invoke()
            }
        }
    }

    fun updatePayment() {
        paymentData?.let { existingPayment ->

            val titleData = payment_title_EditText.text.toString()
            val updatedDate = select_day_TextView.text.toString()
            val updatedAmount = amount_EditText.text.toString().toIntOrNull() ?: 0
            var updatedType = payment_type_Spinner.selectedItem.toString()
            val updatedSubType = payment_subtype_Spinner.selectedItemPosition

            // "수입"을 "income", "지출"을 "expense"로 변환
            updatedType = if (updatedType == "수입") "income" else if(updatedType == "지출") "expense" else "save"

            val payment = Payment(existingPayment.id, titleData, updatedType, updatedSubType, updatedAmount, updatedDate)

            CoroutineScope(Dispatchers.IO).launch {
                // Room을 사용하여 데이터 업데이트
                paymentRepository.updatePaymentById(existingPayment.id, payment)

                // UI 업데이트를 위한 콜백 호출
                withContext(Dispatchers.Main) {
                    dismiss() // 다이얼로그 닫기

                    onSaveCallback!!.invoke()
                }
            }
        }
    }
}
