package com.redhorse.accountbank

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.redhorse.accountbank.data.AppDatabase
import com.redhorse.accountbank.data.Payment
import com.redhorse.accountbank.data.PaymentDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PaymentEditFragment : DialogFragment() {

    private lateinit var paymentDao: PaymentDao

    private lateinit var onSaveCallback: () -> Unit
    private lateinit var payment_title_EditText : EditText
    private lateinit var select_day_TextView : TextView
    private lateinit var select_day_Button : Button
    private lateinit var amount_EditText : EditText
    private lateinit var payment_type_Spinner : Spinner
    private lateinit var insert_Button : Button

    private var isInsertMode : Boolean = false
    private var paymentData: Payment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("PaymentEditFragment", "onCreate called")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("PaymentEditFragment", "onCreateView called")

        val db = AppDatabase.getDatabase(requireContext())
        paymentDao = db.paymentDao()

        arguments?.let {
            paymentData = it.getParcelable("payment") // 전달된 Payment 객체 받기
            Log.e("PaymentEditFragment", "Payment data: $paymentData")
        }

        // Fragment의 레이아웃을 인플레이트합니다.
        val rootView = inflater.inflate(R.layout.fragment_payment_input, container, false)

        payment_title_EditText = rootView.findViewById(R.id.payment_title_edit)
        select_day_TextView = rootView.findViewById(R.id.payment_select_day_text)
        amount_EditText = rootView.findViewById(R.id.payment_amount_edit)
        payment_type_Spinner = rootView.findViewById(R.id.payment_type_spinner)
        insert_Button = rootView.findViewById(R.id.payment_insert_btn)

        // 날짜 버튼 초기화 추가
        select_day_Button = rootView.findViewById(R.id.payment_select_day_btn)
        Log.e("PaymentEditFragment", "select_day_Button initialized")

        if(paymentData == null){
            isInsertMode = true
            Log.e("PaymentEditFragment", "Insert mode enabled")
        }

        // UI에 데이터를 설정하는 로직
        setupUI(rootView)

        insert_Button.setOnClickListener {
            Log.e("PaymentEditFragment", "Insert button clicked")
            if (isInsertMode) {
                insertPayment()
            } else {
                updatePayment()
            }
        }

        select_day_Button.setOnClickListener {
            Log.e("PaymentEditFragment", "Select day button clicked")
            openDatePickerDialog()
        }

        return rootView
    }

    private fun setupUI(rootView: View) {
        paymentData?.let {
            // 내역 표시
            Log.e("PaymentEditFragment", "Setting up UI with paymentData: $it")
            payment_title_EditText.setText(it.title)
            select_day_TextView.setText(it.date)
            amount_EditText.setText(it.amount.toString())
            val isIncome = if (it.type == "income") true else false
            if(!isIncome)
                payment_type_Spinner.setSelection(1)
        } ?: run {
            Log.e("PaymentEditFragment", "No paymentData available, skipping UI setup")
        }
    }

    fun setOnSaveCallback(callback: () -> Unit) {
        this.onSaveCallback = callback
        Log.e("PaymentEditFragment", "onSaveCallback set")
    }

    companion object {
        fun newInstance(payment: Payment): PaymentEditFragment {
            val fragment = PaymentEditFragment()
            val bundle = Bundle()
            bundle.putParcelable("payment", payment) // Payment 객체 전달
            fragment.arguments = bundle
            Log.e("PaymentEditFragment", "Creating new instance with payment: $payment")
            return fragment
        }
    }

    private fun openDatePickerDialog() {
        Log.e("PaymentEditFragment", "Opening DatePickerDialog")
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
                Log.e("PaymentEditFragment", "Selected date: $selectedDate")
            },
            year, month, dayOfMonth
        )

        // 날짜 선택 다이얼로그 표시
        datePickerDialog.show()
    }

    fun insertPayment() {
        Log.e("PaymentEditFragment", "Inserting new payment")

        val titleData = payment_title_EditText.text.toString()
        val date = select_day_TextView.text.toString()
        val amount = amount_EditText.text.toString().toIntOrNull() ?: 0
        var type = payment_type_Spinner.selectedItem.toString()

        if(type.equals("수입"))
            type = "income"
        else
            type = "expense"

        // 새 Payment 객체 생성
        val newPayment = Payment(
            id = 0, // Room에서 자동 생성되므로 0으로 설정
            title = titleData,
            type = type,
            amount = amount,
            date = date
        )

        Log.e("PaymentEditFragment", "New payment created: $newPayment")

        CoroutineScope(Dispatchers.IO).launch {
            paymentDao.insert(newPayment)
            Log.e("PaymentEditFragment", "New payment inserted in DB")

            // UI 업데이트를 위한 콜백 호출
            withContext(Dispatchers.Main) {
                dismiss() // 다이얼로그 닫기
                Log.e("PaymentEditFragment", "Save callback called and dialog dismissed")
            }
        }
    }

    fun updatePayment() {
        paymentData?.let { existingPayment ->
            Log.e("PaymentEditFragment", "Updating payment: $existingPayment")

            val titleData = payment_title_EditText.text.toString()
            val updatedDate = select_day_TextView.text.toString()
            val updatedAmount = amount_EditText.text.toString().toIntOrNull() ?: 0
            var updatedType = payment_type_Spinner.selectedItem.toString()

            // "수입"을 "income", "지출"을 "expense"로 변환
            updatedType = if (updatedType == "수입") "income" else "expense"

            CoroutineScope(Dispatchers.IO).launch {
                // Room을 사용하여 데이터 업데이트
                paymentDao.updatePaymentById(existingPayment.id, titleData, updatedAmount, updatedDate, updatedType)
                Log.e("PaymentEditFragment", "Payment updated in DB")

                // UI 업데이트를 위한 콜백 호출
                withContext(Dispatchers.Main) {
                    dismiss() // 다이얼로그 닫기
                    Log.e("PaymentEditFragment", "Save callback called and dialog dismissed after update")
                }
            }
        }
    }

}
