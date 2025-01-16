package com.redhorse.accountbank.modal

import PaymentRepository
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
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
    private lateinit var onDetailUpdatedCallback: ((String) -> Unit)

    private lateinit var payment_title_EditText : EditText
    private lateinit var select_day_TextView : TextView
    private lateinit var select_day_Button : ImageButton
    private lateinit var amount_EditText : EditText
    private lateinit var payment_type_Spinner : Spinner
    private lateinit var payment_subtype_Spinner : Spinner
    private lateinit var insert_Button : AppCompatButton

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

        val rootView = inflater.inflate(R.layout.modal_payment_input, container, false)

        payment_title_EditText = rootView.findViewById(R.id.payment_title_edit)
        select_day_TextView = rootView.findViewById(R.id.payment_select_day_text)
        amount_EditText = rootView.findViewById(R.id.payment_amount_edit)
        payment_type_Spinner = rootView.findViewById(R.id.payment_type_spinner)
        insert_Button = rootView.findViewById(R.id.payment_modal_insert_btn)
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

        val subtypeAdapter = CustomSpinnerAdapter(requireContext(), paymentTypes)
        payment_subtype_Spinner.adapter = subtypeAdapter

        payment_subtype_Spinner.setSelection(paymentTypes.indexOf("기타"))
    }

    private fun setupUI(rootView: View) {
        paymentData?.let {
            payment_title_EditText.setText(it.title)
            if (isInsertMode) {
                val today = LocalDate.now()
                val formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy-dd-MM"))

                select_day_TextView.text = formattedDate
            } else
                select_day_TextView.setText(it.date)
            amount_EditText.setText(it.amount.toString())

            if (it.type.equals("income")) {
                payment_type_Spinner.setSelection(0)
                updateSubtypeSpinner(0)
            } else if (it.type.equals("expense")) {
                payment_type_Spinner.setSelection(1)
                updateSubtypeSpinner(1)
            } else {
                payment_type_Spinner.setSelection(2)
                updateSubtypeSpinner(2)
            }

        } ?: run {
        }
    }

    fun setOnSaveCallback(callback: () -> Unit) {
        this.onSaveCallback = callback
    }

    fun setOnDetailUpdatedCallback(callback: (String) -> Unit) {
        this.onDetailUpdatedCallback = callback
    }

    companion object {
        fun newInstance(payment: Payment?): PaymentEditFragment {
            val fragment = PaymentEditFragment()
            val bundle = Bundle()
            payment?.let {
                bundle.putParcelable("payment", it)
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

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                select_day_TextView.text = selectedDate
            },
            year, month, dayOfMonth
        )

        datePickerDialog.setOnShowListener {
            val positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negativeButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)

            val isDarkMode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }

            if (isDarkMode) {
                positiveButton.setTextColor(Color.parseColor("#FFFFFFFF"))
                negativeButton.setTextColor(Color.parseColor("#FFFFFFFF"))
            } else {
                positiveButton.setTextColor(Color.parseColor("#FF000000"))
                negativeButton.setTextColor(Color.parseColor("#FF000000"))
            }
        }

        datePickerDialog.show()
    }

    fun insertPayment() {
        val titleData = payment_title_EditText.text.toString()
        val date = select_day_TextView.text.toString()
        val amount = amount_EditText.text.toString().toIntOrNull() ?: 0
        var type = payment_type_Spinner.selectedItem.toString()
        type = if (type == "수입") "income" else if (type == "지출") "expense" else "save"
        val subtype = payment_subtype_Spinner.selectedItemPosition

        if (titleData.isEmpty() || date.isEmpty() || amount == 0 || type.isEmpty()) {
            Toast.makeText(context, "입력값이 모두 채워져야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val newPayment = Payment(
            id = 0,
            title = titleData,
            type = type,
            subtype = subtype,
            amount = amount,
            date = date
        )

        CoroutineScope(Dispatchers.IO).launch {
            paymentRepository.insertOrCreateTableAndInsert(newPayment)

            withContext(Dispatchers.Main) {
                dismiss() // 다이얼로그 닫기

                onSaveCallback!!.invoke()
            }
        }
    }

    fun updatePayment() {
        paymentData?.let { existingPayment ->

            val titleData = payment_title_EditText.text.toString().trim()
            val updatedDate = select_day_TextView.text.toString().trim()
            val updatedAmount = amount_EditText.text.toString().toIntOrNull() ?: 0
            var updatedType = payment_type_Spinner.selectedItem.toString()
            val updatedSubType = payment_subtype_Spinner.selectedItemPosition

            updatedType = if (updatedType == "수입") "income" else if(updatedType == "지출") "expense" else "save"

            if (titleData.isEmpty() || updatedDate.isEmpty() || updatedAmount == 0 || updatedType.isEmpty()) {
                Toast.makeText(context, "입력값이 모두 채워져야 합니다.", Toast.LENGTH_SHORT).show()
                return
            }

            val payment = Payment(existingPayment.id, titleData, updatedType, updatedSubType, updatedAmount, updatedDate)

            CoroutineScope(Dispatchers.IO).launch {
                paymentRepository.updatePaymentById(existingPayment.id, payment)

                withContext(Dispatchers.Main) {
                    dismiss()

                    onSaveCallback!!.invoke()
                    onDetailUpdatedCallback!!.invoke(updatedDate)
                }
            }
        }
    }
}
