package com.redhorse.accountbank.modal

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
        fun newInstance(editData: Payment?, callback: (Payment) -> Unit): RegularlyModal {
            val fragment = RegularlyModal()

            fragment.editpayment = editData
            fragment.onSaveDataCallback = callback
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
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

        val paymentTypes = resources.getStringArray(R.array.payment_types).toList()

        val adapter = CustomSpinnerAdapter(requireContext(), paymentTypes)
        type.adapter = adapter

        type.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                updateSubtypeSpinner(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
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

        val subtypeAdapter = CustomSpinnerAdapter(requireContext(), paymentTypes)
        subtype.adapter = subtypeAdapter

        subtype.setSelection(paymentTypes.indexOf("기타"))
    }

    private fun SetUpdateData() {
        title.text = editpayment!!.title
        day.text = editpayment!!.date
        amount.text = editpayment!!.amount.toString()
        when (editpayment?.type) {
            "income" -> {
                type.setSelection(0)
                updateSubtypeSpinner(0)
            }
            "expense" -> {
                type.setSelection(1)
                updateSubtypeSpinner(1)
            }
            "save" -> {
                type.setSelection(2)
                updateSubtypeSpinner(2)
            }
            else -> {
                type.setSelection(0)
                updateSubtypeSpinner(0)
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

        if (titleData.isEmpty() || date.isEmpty() || amount == 0 || type.isEmpty()) {
            Toast.makeText(context, "입력값이 모두 채워져야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

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