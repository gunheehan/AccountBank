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

    private lateinit var oninsertDataCallback: (Payment) -> Unit

    private lateinit var title : TextView
    private lateinit var day : TextView
    private lateinit var amount : TextView
    private lateinit var type : Spinner
    private lateinit var saveButton : Button

    companion object {
        // 새로운 인스턴스를 만들 때 Payment 객체가 null일 수 있으므로 null 처리 추가
        fun newInstance(callback: (Payment) -> Unit): RegularlyModal {
            val fragment = RegularlyModal()
            fragment.oninsertDataCallback = callback
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

        saveButton.setOnClickListener(){
            OnClickSave()
        }
    }

    private fun OnClickSave()
    {
        val titleData = title.text.toString()
        val date = day.text.toString()
        val amount = amount.text.toString().toIntOrNull() ?: 0
        var type = type.selectedItem.toString()

        type = if (type == "수입") "income" else if(type == "지출") "expense" else "save"

        // 새 Payment 객체 생성
        val newPayment = Payment(
            id = 0, // Room에서 자동 생성되므로 0으로 설정
            title = titleData,
            type = type,
            amount = amount,
            date = date
        )

        oninsertDataCallback.invoke(newPayment)

        dismiss() // 다이얼로그 닫기
    }
}