package com.redhorse.accountbank.modal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.redhorse.accountbank.R
import com.redhorse.accountbank.adapter.CustomSpinnerAdapter
import com.redhorse.accountbank.data.Payment

class RegularlyModal : DialogFragment(){
    private lateinit var onSaveDataCallback: (Payment) -> Unit
    var editpayment : Payment? = null

    private lateinit var title : TextView
    private lateinit var day : TextView
    private lateinit var amount : TextView
    private lateinit var type : Spinner
    private lateinit var subtype : Spinner
    private lateinit var saveButton : Button

    companion object {
        // 새로운 인스턴스를 만들 때 Payment 객체가 null일 수 있으므로 null 처리 추가
        fun newInstance(editData: Payment?, callback: (Payment) -> Unit): RegularlyModal {
            val fragment = RegularlyModal()

            fragment.editpayment = editData
            fragment.onSaveDataCallback = callback
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.modal_regularly_item, container, false)
        SetComponents(rootView)

        return rootView
    }

    private fun SetComponents(view: View)
    {
        title = view.findViewById(R.id.modal_regularly_title_edit_text)
        day = view.findViewById(R.id.modal_regularly_day_text)
        amount = view.findViewById(R.id.modal_regularly_amount_text)
        type = view.findViewById(R.id.modal_regularly_type_spinner)
        subtype = view.findViewById(R.id.modal_regularly_subtype_spinner)

        // string-array에서 값을 가져옴
        val paymentTypes = resources.getStringArray(R.array.payment_types).toList()

// Spinner에 데이터를 직접 설정 (ArrayAdapter 없이)
        val adapter = CustomSpinnerAdapter(requireContext(), paymentTypes)
        type.adapter = adapter

// payment_type_Spinner에 선택 이벤트 리스너 추가
        type.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // 선택된 payment type에 따라 subtype 값을 변경
                updateSubtypeSpinner(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때의 처리
            }
        })

        saveButton = view.findViewById(R.id.modal_regularly_insert_btn)
        saveButton.text = "저장"
        saveButton.setOnClickListener(){
            OnClickSave()
        }

        if(editpayment != null)
        {
            SetUpdateData()
        }
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
        subtype.adapter = subtypeAdapter

        // 기본값은 항상 "기타"로 설정
        subtype.setSelection(paymentTypes.indexOf("기타"))
    }

    private fun SetUpdateData() {
        title.text = editpayment!!.title
        day.text = editpayment!!.date
        amount.text = editpayment!!.amount.toString()
        // PaymentType 설정
        when (editpayment?.type) {
            "income" -> {
                type.setSelection(0)
                updateSubtypeSpinner(0)  // "수입"에 맞는 서브타입 설정
            }
            "expense" -> {
                type.setSelection(1)
                updateSubtypeSpinner(1)  // "지출"에 맞는 서브타입 설정
            }
            "save" -> {
                type.setSelection(2)
                updateSubtypeSpinner(2)  // "적금"에 맞는 서브타입 설정
            }
            else -> {
                type.setSelection(0)
                updateSubtypeSpinner(0)  // 기본 "기타"
            }
        }

        subtype.setSelection(editpayment!!.subtype)
        saveButton.text = "수정"
    }

    private fun OnClickSave()
    {
        var id = 0L
        if(editpayment != null)
            id = editpayment!!.id

        val titleData = title.text.toString()
        val date = day.text.toString()
        val amount = amount.text.toString().toIntOrNull() ?: 0
        var type = type.selectedItem.toString()
        var subtype = subtype.selectedItemPosition

        type = if (type == "수입") "income" else if(type == "지출") "expense" else "save"

        // 새 Payment 객체 생성
        val newPayment = Payment(
            id = id,
            title = titleData,
            type = type,
            subtype = subtype,
            amount = amount,
            date = date
        )

        onSaveDataCallback.invoke(newPayment)

        dismiss() // 다이얼로그 닫기
    }
}