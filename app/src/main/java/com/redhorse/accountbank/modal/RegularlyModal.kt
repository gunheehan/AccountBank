package com.redhorse.accountbank.modal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.redhorse.accountbank.R
import com.redhorse.accountbank.data.Payment

class RegularlyModal : DialogFragment(){
    private lateinit var onSaveDataCallback: (Payment) -> Unit
    var editpayment : Payment? = null

    private lateinit var title : TextView
    private lateinit var day : TextView
    private lateinit var amount : TextView
    private lateinit var type : Spinner
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
        type.setSelection(0)

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

    private fun SetUpdateData() {
        title.text = editpayment!!.title
        day.text = editpayment!!.date
        amount.text = editpayment!!.amount.toString()
        if (editpayment?.type.equals("income")) {
            type.setSelection(0)
        } else if (editpayment!!.type.equals("expense")) {
            type.setSelection(1)
        }else{
            type.setSelection(2)
        }
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

        type = if (type == "수입") "income" else if(type == "지출") "expense" else "save"

        // 새 Payment 객체 생성
        val newPayment = Payment(
            id = id,
            title = titleData,
            type = type,
            amount = amount,
            date = date
        )

        onSaveDataCallback.invoke(newPayment)

        dismiss() // 다이얼로그 닫기
    }
}